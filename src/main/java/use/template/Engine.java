

package use.template;

import use.kit.SyncWriteMap;
import use.template.expr.ast.MethodKit;
import use.template.source.ClassPathSourceFactory;
import use.template.source.FileSourceFactory;
import use.template.source.ISource;
import use.template.source.StringSource;
import use.template.stat.CharTable;
import use.template.stat.Parser;
import use.template.stat.ast.Stat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 Engine 是可以独立使用的, 但它没有
 */
public class Engine {
  private boolean cacheStringTemplate = false;
  public final EngineConfig config;

  private final Map<String, Template> templateCache = new SyncWriteMap<String, Template>(2048, 0.5F);

  /**
   * Create engine without management of JFinal
   */

  /**
   Create engine by engineName without management of JFinal
   */
  private Engine(String name) {
    this(new EngineConfig(name, FileSourceFactory.me));
  }

  public Engine(EngineConfig config) {
    this.config = config;
  }

  public static Engine byFilePath(String name, String file) {
    return new Engine(new EngineConfig(name, new FileSourceFactory(file)));
  }

  public static Engine byClassPath(String name) {
    return new Engine(new EngineConfig(name, ClassPathSourceFactory.me));
  }


  /**
   Get template by file name
   */
  public Template getTemplate(String fileName) {
    Template template = templateCache.get(fileName);
    if (template == null) {
      template = buildTemplateBySourceFactory(fileName, null);
      templateCache.put(fileName, template);
    } else if (template.isModified()) {
      buildTemplateBySourceFactory(fileName, template);
    }
    return template;
  }

  private Template buildTemplateBySourceFactory(String fileName, Template old) {
    // FileSource fileSource = new FileSource(config.getBaseTemplatePath(), fileName, config.encoding);
    ISource source = getSource(fileName);
    return buildTemplateBySource(fileName, source, null, old);
  }

  public ISource getSource(@NotNull String fileName) {
    return config.sourceFactory.getSource(fileName, config.encoding);
  }

  /**
   Get template by string content and do not cache the template
   */
  public Template getTemplateByString(@NotNull String content) {
    return getTemplateByString(content, cacheStringTemplate);
  }

  /**
   Get template by string content

   重要：StringSource 中的 cacheKey = HashKit.md5(content)，也即 cacheKey
   与 content 有紧密的对应关系，当 content 发生变化时 cacheKey 值也相应变化
   因此，原先 cacheKey 所对应的 Template 缓存对象已无法被获取，当 getTemplateByString(String)
   的 String 参数的数量不确定时会引发内存泄漏

   当 getTemplateByString(String, boolean) 中的 String 参数的
   数量可控并且确定时，才可对其使用缓存

   @param content 模板内容
   @param cache   true 则缓存 Template，否则不缓存
   */
  public Template getTemplateByString(@NotNull String content, boolean cache) {
    return getTemplateByString(content, cache, null);
  }

  /**
   source.getContent()传入parser.parse传入Lexer#constructor, 内部会将 source 转成char[], 再到调用scan, 到此生命周期结束
   保留content引用的地方只在ISource中, 是否回收content在于isource的生命周期和cacheKey的取法
   */
  public Template getTemplateByString(@NotNull String content, boolean cache, @Nullable Object root) {
    if (!cache) {
      return buildTemplateBySource(new StringSource(content, cache), root, null);
    }
    //String cacheKey = HashKit.md5(content);
    String cacheKey = content;
    return getTemplateByString(content, cacheKey, root);
  }

  public Template getTemplateByString(@NotNull String content, @NotNull String cacheKey, @Nullable Object root) {
    Template template = templateCache.get(cacheKey);
    if (template == null) {
      template = buildTemplateBySource(new StringSource(content, cacheKey), root, null);
      templateCache.put(cacheKey, template);
    }
    return template;
  }

  /**
   Get template by implementation of ISource
   */
  public Template getTemplate(ISource source) {
    String cacheKey = source.getCacheKey();
    if (cacheKey == null) {  // cacheKey 为 null 则不缓存，详见 ISource.getCacheKey() 注释
      return buildTemplateBySource(source, null, null);
    }

    Template template = templateCache.get(cacheKey);
    if (template == null) {
      template = buildTemplateBySource(source, null, null);
      templateCache.put(cacheKey, template);
    }
    return template;
  }

  /***
   @param old  存在旧template时, 将其刷新
    * */
  public Template buildTemplateBySource(ISource source, @Nullable Object root, Template old) {
    return buildTemplateBySource(null, source, root, old);
  }

  public void rebuild(Template template) {
    // 1. StringSource得到的template不需要此种调用
    // 2. 动态生成的template不需要此种调用, 只有一开始的root template才可以调用
    buildTemplateBySource(template.baseSource(), null, template);
    // 同步模板中所有source
    template.sync();
  }

  private Template buildTemplateBySource(@Nullable String fileName, @NotNull ISource source, @Nullable Object root, @Nullable Template old) {
    //Env env =  new Env(config);
    // 复用env
    Env env = old != null ? old.env : new Env(config);
    Parser parser = new Parser(env, fileName);
    if (getDevMode()) {
      env.addSource(source);
    }
    Stat stat = parser.parse(source);
    if (old != null) {
      old.init(env, stat);
      old.sync();
      return old;
    }
    if (root == null)
      return new Template(env, stat);
    return new Template_ImmutableData(env, stat, root);
  }

  /**
   Add shared function by file
   */
  public Engine addSharedFunction(String fileName) {
    config.addSharedFunction(fileName);
    return this;
  }

  /**
   Add shared function by ISource
   */
  public Engine addSharedFunction(ISource source) {
    config.addSharedFunction(source);
    return this;
  }

  /**
   Add shared function by files
   */
  public Engine addSharedFunction(String... fileNames) {
    config.addSharedFunction(fileNames);
    return this;
  }

  /**
   Add shared function by string content
   */
  public Engine addSharedFunctionByString(String content) {
    config.addSharedFunctionByString(content);
    return this;
  }

  /**
   Add shared object
   */
  public Engine addSharedObject(String name, Object object) {
    config.addSharedObject(name, object);
    return this;
  }

  public Engine removeSharedObject(String name) {
    config.removeSharedObject(name);
    return this;
  }

  /**
   添加枚举类型，便于在模板中使用

   <pre>
   例子：
   1：定义枚举类型
   public enum UserType {

   ADMIN,
   USER;

   public String hello() {
   return "hello";
   }
   }

   2：配置
   engine.addEnum(UserType.class);

   3：模板中使用
   ### 以下的对象 u 通过 Controller 中的 setAttr("u", UserType.ADMIN) 传递
   #if( u == UserType.ADMIN )
   #(UserType.ADMIN)

   #(UserType.ADMIN.name())

   #(UserType.ADMIN.hello())
   #end

   </pre>
   */
  public Engine addEnum(Class<? extends Enum<?>> enumClass) {
    Map<String, Enum<?>> map = new java.util.LinkedHashMap<>();
    Enum<?>[] es = enumClass.getEnumConstants();
    for (Enum<?> e : es) {
      map.put(e.name(), e);
    }
    return addSharedObject(enumClass.getSimpleName(), map);
  }


  /**
   Remove template cache by cache key
   */
  public void removeTemplateCache(String cacheKey) {
    templateCache.remove(cacheKey);
  }

  public void reload() {
    for (Template value : templateCache.values()) {
      if (value.isModified()) {
        rebuild(value);
      }
    }
  }

  public int getTemplateCacheSize() {
    return templateCache.size();
  }

  // Engine config below ---------


  /**
   设置 true 为开发模式，支持模板文件热加载
   设置 false 为生产模式，不支持模板文件热加载，以达到更高的性能
   */
  public Engine setDevMode(boolean devMode) {
    if (templateCache.size() != 0)
      throw new RuntimeException("engine启动后不再支持设置devMode");
    this.config.setDevMode(devMode);
    return this;
  }

  public boolean getDevMode() {
    return config.isDevMode();
  }

  /**
   配置是否缓存字符串模板，也即是否缓存通过 getTemplateByString(String content)
   方法获取的模板，默认配置为 false
   */
  public Engine setCacheStringTemplate(boolean cacheStringTemplate) {
    this.cacheStringTemplate = cacheStringTemplate;
    return this;
  }


  public static void addExtensionMethod(Class<?> targetClass, Object objectOfExtensionClass) {
    MethodKit.addExtensionMethod(targetClass, objectOfExtensionClass);
  }

  public static void addExtensionMethod(Class<?> targetClass, Class<?> extensionClass) {
    MethodKit.addExtensionMethod(targetClass, extensionClass);
  }

  public static void removeExtensionMethod(Class<?> targetClass, Object objectOfExtensionClass) {
    MethodKit.removeExtensionMethod(targetClass, objectOfExtensionClass);
  }

  public static void removeExtensionMethod(Class<?> targetClass, Class<?> extensionClass) {
    MethodKit.removeExtensionMethod(targetClass, extensionClass);
  }

  /**
   * 添加 FieldGetter 实现类到指定的位置
   *
   * 系统当前默认 FieldGetter 实现类及其位置如下：
   * GetterMethodFieldGetter  ---> 调用 getter 方法取值
   * RealFieldGetter			---> 直接获取 public 型的 object.field 值
   * ModelFieldGetter			---> 调用 Model.get(String) 方法取值
   * RecordFieldGetter			---> 调用 Record.get(String) 方法取值
   * MapFieldGetter			---> 调用 Map.get(String) 方法取值
   * ArrayLengthGetter			---> 获取数组长度
   *
   * 根据以上次序，如果要插入 IsMethodFieldGetter 到 GetterMethodFieldGetter
   * 之后的代码如下：
   * Engine.addFieldGetter(1, new IsMethodFieldGetter());
   *
   * 注：IsMethodFieldGetter 系统已经提供，只是默认没有启用。该实现类通过调用
   *    target.isXxx() 方法获取 target.xxx 表达式的值，其中 isXxx() 返回值
   *    必须是 Boolean/boolean 类型才会被调用
   */


  /**
   设置为 true 支持表达式、变量名、方法名、模板函数名使用中文
   */
  public static void setChineseExpression(boolean enable) {
    CharTable.setChineseExpression(enable);
  }

  public Object eval(Object o, String expr, boolean cache) {
    return getTemplateByString(expr, cache, o).eval();
  }
}


