
package use.template.stat.ast;

import use.template.io.Writer;
import use.template.Env;
import use.template.TemplateException;
import use.template.expr.ast.ExprList;
import use.template.stat.Scope;

/**
 * Call 调用模板函数，两种用法：
 * 1：常规调用
 *    #@funcName(p1, p2, ..., pn)
 * 2：安全调用，函数被定义才调用，否则跳过
 *    #@funcName?(p1, p2, ..., pn)
 * 
 * 注意：在函数名前面引入 '@' 字符是为了区分模板函数和指令
 */
public class Call extends Stat {
	
	private String funcName;
	private ExprList exprList;
	private boolean callIfDefined;
	
	public Call(String funcName, ExprList exprList, boolean callIfDefined) {
		this.funcName = funcName;
		this.exprList = exprList;
		this.callIfDefined = callIfDefined;
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		Define function = env.getFunction(funcName);
		if (function != null) {
			function.call(env, scope, exprList, writer);	
		} else if (callIfDefined) {
			return ;
		} else {
			throw new TemplateException("Template function not defined: " + funcName, location);
		}
	}
}

