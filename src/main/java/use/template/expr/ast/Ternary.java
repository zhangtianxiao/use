
package use.template.expr.ast;

import use.template.Env;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

/**
 Ternary
 */
public class Ternary extends Expr {

  private final Expr cond;
  private final Expr exprOne;
  private final Expr exprTwo;

  /**
   cond ? exprOne : exprTwo
   */
  public Ternary(Expr cond, Expr exprOne, Expr exprTwo, Location location) {
    if (cond == null || exprOne == null || exprTwo == null) {
      throw new ParseException("The parameter of ternary expression can not be blank", location);
    }
    this.cond = cond;
    this.exprOne = exprOne;
    this.exprTwo = exprTwo;
    this.location = location;
  }

  @Override
public Object eval(Scope scope, Env env) {
    return Logic.isTrue(cond.eval(scope,env)) ? exprOne.eval(scope,env) : exprTwo.eval(scope,env);
  }
}








