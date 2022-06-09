

package use.template;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import use.template.source.ISource;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.ast.Define;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import static use.kit.Helper.getLogger;

/**
 Env

 1：解析时存放 #define 定义的模板函数
 2：运行时提供 #define 定义的模板函数
 3：每个 Template 对象持有一个 Env 对象 */
public class Env {
  private static final Logger log = getLogger(Env.class);

  public final EngineConfig config;
  protected Map<String, Define> functionMap = new HashMap<String, Define>(16, 0.5F);

  // 代替 Template 持有该属性，便于在 #include 指令中调用 Env.addSource()
  protected LinkedHashSet<ISource> sourceList = null;

  public Env(EngineConfig engineConfig) {
    log.debug("Env创建: " + this.getClass() + " " + System.identityHashCode(this));
    this.config = engineConfig;
  }

  public boolean isDevMode() {
    return config.isDevMode();
  }

  @Nullable
  public ISource baseSource() {
    if (sourceList != null) {
      Iterator<ISource> iterator = sourceList.iterator();
      if (iterator.hasNext())
        return iterator.next();
    }
    return null;
  }

  /**
   Add template function
   */
  public void addFunction(Define function) {
    String fn = function.getFunctionName();
    if (functionMap.containsKey(fn)) {
      Define previous = functionMap.get(fn);
      throw new ParseException(
        "Template function \"" + fn + "\" already defined in " +
          getAlreadyDefinedLocation(previous.getLocation()),
        function.getLocation()
      );
    }
    functionMap.put(fn, function);
  }

  private String getAlreadyDefinedLocation(Location loc) {
    StringBuilder buf = new StringBuilder();
    if (loc.getTemplateFile() != null) {
      buf.append(loc.getTemplateFile()).append(", line ").append(loc.getRow());
    }
    else {
      buf.append("string template line ").append(loc.getRow());
    }
    return buf.toString();
  }

  /**
   Get function of current template first, getting shared function if null before
   */
  public Define getFunction(String functionName) {
    Define func = functionMap.get(functionName);
    return func != null ? func : config.getSharedFunction(functionName);
  }

  /**
   For EngineConfig.addSharedFunction(...) only
   */
  Map<String, Define> getFunctionMap() {
    return functionMap;
  }

  /**
   本方法用于在 devMode 之下，判断当前 Template 以及其下 #include 指令
   所涉及的所有 ISource 对象是否被修改，以便于在 devMode 下重新加载

   sourceList 属性用于存放主模板以及 #include 进来的模板所对应的
   ISource 对象
   */
  public boolean isSourceListModified() {
    if (sourceList != null) {
      for (ISource source : sourceList) {
        if (source.isModified()) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   添加本 Template 的 ISource，以及该 Template 使用 include 包含进来的所有 ISource
   以便于在 devMode 之下判断该 Template 是否被 modified，进而 reload 该 Template
   */
  public void addSource(ISource source) {
    if (sourceList == null)
      sourceList = new LinkedHashSet<>();
    sourceList.add(source);
  }
}



