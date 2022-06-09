package use.template;

import use.beans.Beans;
import use.kit.ObjectPool;
import use.kit.ReflectKit;
import use.kit.ResourceMode;
import use.kit.StrKit;
import use.template.expr.ast.Arith;
import use.template.expr.ast.ExprList;
import use.template.expr.ast.SharedMethodKit;
import use.template.ext.directive.*;
import use.template.ext.sharedmethod.SharedMethodLib;
import use.template.source.*;
import use.template.stat.*;
import use.template.stat.ast.Define;
import use.template.stat.ast.Output;

import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

/**
 EngineConfig
 */
public class EngineConfig {

  public static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
  public final String name;
  public final Beans beans;
  public final ObjectPool<Scope> scopePool;

  Compressor compressor = null;

  private Map<String, Define> sharedFunctionMap = createSharedFunctionMap();    // new HashMap<String, Define>(512, 0.25F);
  private final List<ISource> sharedFunctionSourceList = new ArrayList<ISource>();    // for devMode only

  Map<String, Object> sharedObjectMap = null;

  private OutputDirectiveFactory outputDirectiveFactory = OutputDirectiveFactory.me;
  public final ISourceFactory sourceFactory;
  private final Map<String, Supplier<? extends Directive>> directiveMap = new HashMap(64, 0.5F);
  private final SharedMethodKit sharedMethodKit = new SharedMethodKit();

  // 保留指令所在行空白字符的指令
  private final Set<String> keepLineBlankDirectives = new HashSet<>();

  private boolean devMode = false;
  private boolean reloadModifiedSharedFunctionInDevMode = true;
  public final Charset encoding = DEFAULT_ENCODING;
  private String datePattern = "yyyy-MM-dd HH:mm";

  // 浮点数输出与运算时使用的舍入模式，默认值为 "四舍五入"
  private RoundingMode roundingMode = RoundingMode.HALF_UP;

  /*public EngineConfig() {
    this(ClassPathSourceFactory.me, Beans.me, null);
  }*/

  public EngineConfig(String name, ISourceFactory sourceFactory) {
    this(name, sourceFactory, Beans.me, null);
  }


  public EngineConfig(String name, ISourceFactory sourceFactory, Beans beans, ObjectPool<Scope> scopePool) {
    this.name = name;
    this.sourceFactory = sourceFactory;
    this.beans = beans;
    if (scopePool == null)
      scopePool = ObjectPool.create(name, Scope.maker, Scope.clearer, -1);
    this.scopePool = scopePool;

    // 内置指令 #() 与 #include() 需要配置，保留指令所在行前后空白字符以及行尾换行字符 '\n'
    setKeepLineBlank("output", true);
    setKeepLineBlank("include", true);

    // Add official directive of Template Engine
    addDirective("render", RenderDirective.class, true);
    addDirective("escape", EscapeDirective.class, true);
    addDirective("random", RandomDirective.class, true);
    addDirective("number", NumberDirective.class, true);

    addDirective("call", CallDirective.class, false);
    addDirective("json", JsonDirective.class, false);

    // Add official shared method of Template Engine
    addSharedMethod(new SharedMethodLib());
  }


  /**
   Add shared function with file
   */
  public void addSharedFunction(String fileName) {
    fileName = fileName.replace("\\", "/");
    ISource source = sourceFactory.getSource(fileName, encoding);
    doAddSharedFunction(source, fileName);
  }

  private synchronized void doAddSharedFunction(ISource source, String fileName) {
    Env env = new Env(this);
    new Parser(env, fileName).parse(source);
    addToSharedFunctionMap(sharedFunctionMap, env);
    if (devMode) {
      sharedFunctionSourceList.add(source);
      env.addSource(source);
    }
  }

  /**
   Add shared function with files
   */
  public void addSharedFunction(String... fileNames) {
    for (String fileName : fileNames) {
      addSharedFunction(fileName);
    }
  }

  /**
   Add shared function by string content
   */
  public void addSharedFunctionByString(String content) {
    // content 中的内容被解析后会存放在 Env 之中，而 StringSource 所对应的
    // Template 对象 isModified() 始终返回 false，所以没有必要对其缓存
    StringSource stringSource = new StringSource(content, false);
    doAddSharedFunction(stringSource, null);
  }

  /**
   Add shared function by ISource
   */
  public void addSharedFunction(ISource source) {
    String fileName = source.fileName();
    doAddSharedFunction(source, fileName);
  }

  private void addToSharedFunctionMap(Map<String, Define> sharedFunctionMap, Env env) {
    Map<String, Define> funcMap = env.getFunctionMap();
    for (Entry<String, Define> e : funcMap.entrySet()) {
      if (sharedFunctionMap.containsKey(e.getKey())) {
        throw new IllegalArgumentException("Template function already exists : " + e.getKey());
      }
      Define func = e.getValue();
      if (devMode) {
        func.setEnvForDevMode(env);
      }
      sharedFunctionMap.put(e.getKey(), func);
    }
  }

  /**
   Get shared function by Env
   SharedFunction始终都要从Map中取出, 没有根据source动态更新的必要, 不像Template可以提前取出来
   */
  Define getSharedFunction(String functionName) {
    Define func = sharedFunctionMap.get(functionName);
    if (func == null) {
      /**
       * 如果 func 最初未定义，但后续在共享模板文件中又被添加进来
       * 此时在本 if 分支中无法被感知，仍然返回了 null
       *
       * 但共享模板文件会在后续其它的 func 调用时被感知修改并 reload
       * 所以本 if 分支不考虑处理模板文件中追加 #define 的情况
       *
       * 如果要处理，只能是每次在 func 为 null 时，判断 sharedFunctionSourceList
       * 中的模板是否被修改过，再重新加载，不优雅
       */
      return null;
    }

    if (devMode && reloadModifiedSharedFunctionInDevMode) {
      if (func.isSourceModifiedForDevMode()) {
        synchronized (this) {
          func = sharedFunctionMap.get(functionName);
          if (func.isSourceModifiedForDevMode()) {
            reloadSharedFunctionSourceList();
            func = sharedFunctionMap.get(functionName);
          }
        }
      }
    }
    return func;
  }

  /**
   Reload shared function source list

   devMode 要照顾到 sharedFunctionFiles，所以暂不提供
   removeSharedFunction(String functionName) 功能
   开发者可直接使用模板注释功能将不需要的 function 直接注释掉
   */
  private synchronized void reloadSharedFunctionSourceList() {
    Map<String, Define> newMap = createSharedFunctionMap();
    for (int i = 0, size = sharedFunctionSourceList.size(); i < size; i++) {
      ISource source = sharedFunctionSourceList.get(i);
      String fileName = source.fileName();

      Env env = new Env(this);
      new Parser(env, fileName).parse(source);
      addToSharedFunctionMap(newMap, env);
      if (devMode) {
        env.addSource(source);
      }
    }
    this.sharedFunctionMap = newMap;
  }

  private Map<String, Define> createSharedFunctionMap() {
    return new HashMap<String, Define>(512, 0.25F);
  }

  public synchronized void addSharedObject(String name, Object object) {
    if (sharedObjectMap == null) {
      sharedObjectMap = new HashMap<String, Object>(64, 0.25F);
    } else if (sharedObjectMap.containsKey(name)) {
      throw new IllegalArgumentException("Shared object already exists: " + name);
    }
    sharedObjectMap.put(name, object);
  }

  public Map<String, Object> getSharedObjectMap() {
    return sharedObjectMap;
  }

  public synchronized void removeSharedObject(String name) {
    if (sharedObjectMap != null) {
      sharedObjectMap.remove(name);
    }
  }

  /**
   Set output directive factory
   */
  public void setOutputDirectiveFactory(OutputDirectiveFactory outputDirectiveFactory) {
    if (outputDirectiveFactory == null) {
      throw new IllegalArgumentException("outputDirectiveFactory can not be null");
    }
    this.outputDirectiveFactory = outputDirectiveFactory;
  }

  public Output getOutputDirective(ExprList exprList, Location location) {
    return outputDirectiveFactory.getOutputDirective(exprList, location);
  }

  /**
   Invoked by Engine only
   */
  void setDevMode(boolean devMode) {
    this.devMode = devMode;
  }

  public boolean isDevMode() {
    return devMode;
  }


  public ISource getSource(String name) {
    return sourceFactory.getSource(name, encoding);
  }


  public void setDatePattern(String datePattern) {
    if (StrKit.isBlank(datePattern)) {
      throw new IllegalArgumentException("datePattern can not be blank");
    }
    this.datePattern = datePattern;
  }

  public String getDatePattern() {
    return datePattern;
  }

  public void setReloadModifiedSharedFunctionInDevMode(boolean reloadModifiedSharedFunctionInDevMode) {
    this.reloadModifiedSharedFunctionInDevMode = reloadModifiedSharedFunctionInDevMode;
  }

  public void addDirective(String directiveName, Supplier<? extends Directive> directiveClass, boolean keepLineBlank) {
    if (StrKit.isBlank(directiveName)) {
      throw new IllegalArgumentException("directive name can not be blank");
    }
    if (directiveClass == null) {
      throw new IllegalArgumentException("directiveClass can not be null");
    }
    if (directiveMap.containsKey(directiveName)) {
      throw new IllegalArgumentException("directive already exists : " + directiveName);
    }

    directiveMap.put(directiveName, directiveClass);
    if (keepLineBlank) {
      keepLineBlankDirectives.add(directiveName);
    }
  }

  public synchronized void addDirective(String directiveName, Class<? extends Directive> directiveClass, boolean keepLineBlank) {
    addDirective(directiveName, () -> ReflectKit.newInstance(directiveClass), keepLineBlank);
  }

  public void addDirective(String directiveName, Class<? extends Directive> directiveClass) {
    addDirective(directiveName, directiveClass, false);
  }


  public void addDirective(String directiveName, Directive directive) {
    addDirective(directiveName, () -> directive, false);
  }


  public Supplier<? extends Directive> getDirective(String directiveName) {
    return directiveMap.get(directiveName);
  }

  public void removeDirective(String directiveName) {
    directiveMap.remove(directiveName);
    keepLineBlankDirectives.remove(directiveName);
  }

  public void setKeepLineBlank(String directiveName, boolean keepLineBlank) {
    if (keepLineBlank) {
      keepLineBlankDirectives.add(directiveName);
    } else {
      keepLineBlankDirectives.remove(directiveName);
    }
  }

  public Set<String> getKeepLineBlankDirectives() {
    return keepLineBlankDirectives;
  }

  /**
   Add shared method from object
   */
  public void addSharedMethod(Object sharedMethodFromObject) {
    sharedMethodKit.addSharedMethod(sharedMethodFromObject);
  }

  /**
   Add shared method from class
   */
  public void addSharedMethod(Class<?> sharedMethodFromClass) {
    sharedMethodKit.addSharedMethod(sharedMethodFromClass);
  }

  /**
   Add shared static method of Class
   */
  public void addSharedStaticMethod(Class<?> sharedStaticMethodFromClass) {
    sharedMethodKit.addSharedStaticMethod(sharedStaticMethodFromClass);
  }

  /**
   Remove shared Method with method name
   */
  public void removeSharedMethod(String methodName) {
    sharedMethodKit.removeSharedMethod(methodName);
  }

  /**
   Remove shared Method of the Class
   */
  public void removeSharedMethod(Class<?> sharedClass) {
    sharedMethodKit.removeSharedMethod(sharedClass);
  }

  /**
   Remove shared Method
   */
  public void removeSharedMethod(Method method) {
    sharedMethodKit.removeSharedMethod(method);
  }

  public SharedMethodKit getSharedMethodKit() {
    return sharedMethodKit;
  }

  public void setCompressor(Compressor compressor) {
    this.compressor = compressor;
  }

  public Compressor getCompressor() {
    return compressor;
  }

  /**
   设置 #number 指令与 Arith 中浮点数的舍入规则，默认为 RoundingMode.HALF_UP "四舍五入"
   */
  public void setRoundingMode(RoundingMode roundingMode) {
    this.roundingMode = roundingMode;
    Arith.setBigDecimalDivideRoundingMode(roundingMode);
  }

  public RoundingMode getRoundingMode() {
    return roundingMode;
  }
}





