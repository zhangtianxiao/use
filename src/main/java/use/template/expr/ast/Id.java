
package use.template.expr.ast;

import use.template.Env;
import use.template.stat.Scope;

/**
 Id
 */
public class Id extends Expr {

  private final String id;

  public Id(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public Object eval(Scope scope, Env env) {
    Object o = scope.get(id);
    return o;
  }

  /**
   Id.toString() 后续版本不能变动，已有部分第三方依赖此方法
   */
  public String toString() {
    return id;
  }
}


