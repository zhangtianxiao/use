
package use.template.stat.ast;

import use.template.io.Writer;
import use.template.Env;
import use.template.TemplateException;
import use.template.expr.ast.Expr;
import use.template.expr.ast.ExprList;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

/**
 * Case
 */
public class Case extends Stat implements CaseSetter {
	
	private Expr[] exprArray;
	private Stat stat;
	private Case nextCase;
	
	public Case(ExprList exprList, StatList statList, Location location) {
		if (exprList.length() == 0) {
			throw new ParseException("The parameter of #case directive can not be blank", location);
		}
		
		this.exprArray = exprList.getExprArray();
		this.stat = statList.getActualStat();
	}
	
	public void setNextCase(Case nextCase) {
		this.nextCase = nextCase;
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		throw new TemplateException("#case 指令的 exec 不能被调用", location);
	}
	
	boolean execIfMatch(Object switchValue, Env env, Scope scope, Writer writer) {
		if (exprArray.length == 1) {
			Object value = exprArray[0].eval(scope);
			
			// 照顾 null == null 以及数值比较小的整型数据比较
			if (value == switchValue) {
				stat.exec(env, scope, writer);
				return true;
			}
			
			if (value != null && value.equals(switchValue)) {
				stat.exec(env, scope, writer);
				return true;
			}
		} else {
			for (Expr expr : exprArray) {
				Object value = expr.eval(scope);
				
				// 照顾 null == null 以及数值比较小的整型数据比较
				if (value == switchValue) {
					stat.exec(env, scope, writer);
					return true;
				}
				
				if (value != null && value.equals(switchValue)) {
					stat.exec(env, scope, writer);
					return true;
				}
			}
		}
		
		return nextCase != null ? nextCase.execIfMatch(switchValue, env, scope, writer) : false;
	}
}


