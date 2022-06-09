
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
 * SetLocal 设置局部变量
 * 
 * 通常用于 #define #include 指令内部需要与外层作用域区分，以便于定义重用型模块的场景
 * 也常用于 #for 循环内部的临时变量
 */
public class SetLocal  extends Stat {
	
	final Expr expr;
	
	public SetLocal(ExprList exprList, Location location) {
		if (exprList.length() == 0) {
			throw new ParseException("The parameter of #varLocal directive can not be blank", location);
		}
		
		/* 放开对表达式类型的限定
		for (Expr expr : exprList.getExprArray()) {
			if ( !(expr instanceof Assign || expr instanceof IncDec) ) {
				throw new ParseException("#varLocal directive only supports assignment expressions", location);
			}
		}*/
		
		this.expr = exprList.getActualExpr();
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		Ctrl ctrl = scope.getCtrl();
		try {
			ctrl.setLocalAssignment();
			expr.eval(scope);
		} finally {
			ctrl.setWisdomAssignment();
		}
	}
}





