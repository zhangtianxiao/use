
package use.template.expr.ast;

import use.template.Env;
import use.template.TemplateException;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

/**
 forCtrl : ID : expression
 | exprList? ';' expr? ';' exprList?

 两种用法
 1：#for(id : list) #end
 #for(entry : map) #end

 2：#for(init; cond; update) #end
 */
public class ForCtrl extends Expr {

  private final String id;
  private final Expr expr;

  private final Expr init;
  private final Expr cond;
  private final Expr update;

  /**
   ID : expr
   */
  public ForCtrl(Id id, Expr expr, Location location) {
    if (expr == null) {
      throw new ParseException("The iterator target of #for statement can not be null", location);
    }
    this.id = id.getId();
    this.expr = expr;
    this.init = null;
    this.cond = null;
    this.update = null;
    this.location = location;
  }

  /**
   exprList? ';' expr? ';' exprList?
   */
  public ForCtrl(ExprList init, Expr cond, ExprList update, Location location) {
    this.init = init.getActualExpr();
    this.cond = cond;
    this.update = update.getActualExpr();
    this.id = null;
    this.expr = null;
    this.location = location;
  }

  public boolean isIterator() {
    return id != null;
  }

  public String getId() {
    return id;
  }

  public Expr getExpr() {
    return expr;
  }

  public Expr getInit() {
    return init;
  }

  public Expr getCond() {
    return cond;
  }

  public Expr getUpdate() {
    return update;
  }

  @Override
  public Object eval(Scope scope, Env env) {
    throw new TemplateException("The eval(Scope scope) method can not be invoked", location);
  }
}


