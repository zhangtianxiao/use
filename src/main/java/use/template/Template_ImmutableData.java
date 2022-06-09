package use.template;

import use.template.stat.Scope;
import use.template.stat.ast.Stat;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public class Template_ImmutableData extends Template {
  final Scope scope;
  final Map<String, Object> data;

  public Template_ImmutableData(Env env, Stat ast, Object root) {
    super(env, ast);
    Scope scope = new Scope();
    //Map<String, Object> data = Collections.singletonMap("it", root);
    //data = Collections.singletonMap("it", root);
    if (root instanceof Map) {
      data = Map.copyOf((Map) root);
    } else
      data = Collections.singletonMap("it", root);

    scope.init(data, env.config);
    this.scope = scope;
  }

  @Nullable
  @Override
  public Object eval() {
    /*return env.engineConfig.scopeFactory.useEx(scope -> {
      scope.init(env.engineConfig, data);
      return ((Output) ((StatList) ast).getStat(0)).eval(scope);
    });*/
    return output.eval(scope);
  }
}