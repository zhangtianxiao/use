
package use.template.stat.ast;

import use.template.io.Writer;
import use.template.Env;
import use.template.expr.ast.Expr;
import use.template.expr.ast.ExprList;
import use.template.stat.Ctrl;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

/**
 * SetGlobal 设置全局变量，全局作用域是指本次请求的整个 template
 * 
 * 适用于极少数的在内层作用域中希望直接操作顶层作用域的场景
 */
public class SetGlobal  extends Stat {
	
	private Expr expr;
	
	public SetGlobal(ExprList exprList, Location location) {
		if (exprList.length() == 0) {
			throw new ParseException("The parameter of #varGlobal directive can not be blank", location);
		}
		
		/* 放开对表达式类型的限定
		for (Expr expr : exprList.getExprArray()) {
			if ( !(expr instanceof Assign || expr instanceof IncDec) ) {
				throw new ParseException("#varGlobal directive only supports assignment expressions", location);
			}
		}*/
		
		this.expr = exprList.getActualExpr();
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		Ctrl ctrl = scope.getCtrl();
		try {
			ctrl.setGlobalAssignment();
			expr.eval(scope,env);
		} finally {
			ctrl.setWisdomAssignment();
		}
	}
}





