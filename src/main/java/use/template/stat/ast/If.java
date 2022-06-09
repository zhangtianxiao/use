
package use.template.stat.ast;

import use.template.io.Writer;
import use.template.Env;
import use.template.expr.ast.Expr;
import use.template.expr.ast.ExprList;
import use.template.expr.ast.Logic;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

/**
 If
 */
public class If extends Stat {

  private final Expr cond;
  private final Stat stat;
  private Stat elseIfOrElse;

  public If(ExprList cond, StatList statList, Location location) {
    if (cond.length() == 0) {
      throw new ParseException("The condition expression of #if statement can not be blank", location);
    }
    this.cond = cond.getActualExpr();
    this.stat = statList.getActualStat();
  }

  /**
   take over setStat(...) method of super class
   */
  @Override
  public void setStat(Env env, Stat elseIfOrElse) {
    this.elseIfOrElse = elseIfOrElse;
  }

  public void exec(Env env, Scope scope, Writer writer) {
    if (Logic.isTrue(cond.eval(scope))) {
      stat.exec(env, scope, writer);
    } else if (elseIfOrElse != null) {
      elseIfOrElse.exec(env, scope, writer);
    }
  }
}



