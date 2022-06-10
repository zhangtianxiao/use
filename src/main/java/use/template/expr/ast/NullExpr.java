
package use.template.expr.ast;

import use.template.Env;
import use.template.stat.Scope;

/**
 NullExpr
 */
public class NullExpr extends Expr {

  public static final NullExpr me = new NullExpr();

  private NullExpr() {
  }

  @Override
  public Object eval(Scope scope, Env env) {
    return null;
  }
}



