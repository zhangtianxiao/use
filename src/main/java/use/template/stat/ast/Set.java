
package use.template.stat.ast;

import use.template.io.Writer;
import use.template.Env;
import use.template.expr.ast.Expr;
import use.template.expr.ast.ExprList;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

/**
 Set 赋值，从内向外作用域查找变量，找到则替换变量值，否则在顶层作用域赋值

 用法：
 1：#set(k = v)
 2：#set(k1 = v1, k2 = v2, ..., kn = vn)
 3：#set(x = 1+2)
 4：#set(x = 1+2, y = 3>4, ..., z = c ? a : b)
 */
public class Set extends Stat {

  private final Expr expr;

  public Set(ExprList exprList, Location location) {
    if (exprList.length() == 0) {
      throw new ParseException("The parameter of #set directive can not be blank", location);
    }
		
		/* 放开对表达式类型的限定
		for (Expr expr : exprList.getExprArray()) {
			if ( !(expr instanceof Assign || expr instanceof IncDec) ) {
				throw new ParseException("#set directive only supports assignment expressions", location);
			}
		}*/

    this.expr = exprList.getActualExpr();
  }

  public void exec(Env env, Scope scope, Writer writer) {
    scope.getCtrl().setWisdomAssignment();
    expr.eval(scope, env);
  }
}

