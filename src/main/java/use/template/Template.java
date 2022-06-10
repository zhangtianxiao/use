

package use.template;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import use.kit.ObjectPool;
import use.template.io.TextWriter;
import use.template.io.Writer;
import use.template.source.ISource;
import use.template.stat.Scope;
import use.template.stat.ast.Output;
import use.template.stat.ast.Stat;
import use.template.stat.ast.StatList;
import use.template.stat.ast.Text;

import java.util.Map;
import java.util.Objects;

/**
 Template

 用法：
 Template template = Engine.use().getTemplate(...);
 template.render(data, writer);
 template.renderToString(data);
 */
public class Template {

  protected Env env;
  protected Stat ast;

  @Nullable
  protected Output output;

  public Template() {
  }

  public Template(Env env, Stat ast) {
    this.init(env, ast);
  }

  public void init(Env env, Stat ast) {
    this.env = env;
    this.ast = ast;
    output = extractOutput();
  }

  @Nullable
  private Output extractOutput() {
    if (ast instanceof StatList statList) {
      if (statList.length() > 0) {
        Stat stat = statList.getStat(0);
        if (stat instanceof Output) return (Output) stat;
      }
    }
    return null;
  }

  public Stat getAst() {
    return ast;
  }

  @Nullable
  public Object eval() {
    return output.eval(null, env);
    //return  a+b;
  }

  @Nullable
  public Object eval(Map<?, ?> data) {
    EngineConfig engineConfig = env.config;
    ObjectPool<Scope> scopePool = engineConfig.scopePool;
    Scope scope = scopePool.get();
    scope.init(data, engineConfig);
    try {
      return output.eval(scope, env);
    } finally {
      scopePool.recycle(scope);
    }
  }

  public boolean isModified() {
    return env.isSourceListModified();
  }

  public void sync() {
    for (ISource iSource : env.sourceList) {
      iSource.getContent();
    }
  }

  /**
   渲染到 OutputStream 中去
   */
  public void render(@NotNull Map<?, ?> data) {
    render(data, Writer.empty());
  }

  public String renderToString(@NotNull Map<?, ?> data, Writer writer) {
    render(data, writer);
    String s = writer.toUTF8();
    return s;
  }

  public void render(@NotNull Map<?, ?> data, Writer writer) {
    EngineConfig config = env.config;
    ObjectPool<Scope> factory = config.scopePool;
    Scope scope = factory.get();
    scope.init(data, config);
    try {
      ast.exec(env, scope, writer);
    } finally {
      factory.recycle(scope);
    }
    //ast.exec(env, new Scope(data, env.engineConfig.sharedObjectMap,env.engineConfig.fg), writer);
  }

  public String renderToString(@NotNull Map<?, ?> data) {
    TextWriter textWriter = TextWriter.pool.get();
    try {
      render(data, textWriter);
      return textWriter.toUTF8();
    } finally {
      TextWriter.pool.recycle(textWriter);
    }
  }

  @Nullable
  public ISource baseSource() {
    return env.baseSource();
  }

  public boolean immutable() {
    return false;
  }
}





