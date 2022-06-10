
package use.template.ext.directive;

import use.template.io.Writer;
import use.template.Directive;
import use.template.Env;
import use.template.TemplateException;
import use.template.expr.ast.Const;
import use.template.expr.ast.Expr;
import use.template.expr.ast.ExprList;
import use.template.stat.ParseException;
import use.template.stat.Scope;
import use.template.stat.ast.Define;

import java.util.ArrayList;

/**
 * CallDirective 动态调用模板函数
 * 
 * 模板函数的名称与参数都可以动态指定，提升模板函数调用的灵活性
 * 
 * 例如：
 *     #call(funcName, p1, p2, ..., pn)
 *     其中 funcName，为函数名，p1、p2、pn 为被调用函数所使用的参数
 * 
 * 
 * 如果希望模板函数不存在时忽略其调用，添加常量值 true 在第一个参数位置即可
 * 例如：
 *     #call(true, funcName, p1, p2, ..., pn)
 * 
 * 
 * TODO 后续优化看一下 ast.Call.java
 */
public class CallDirective extends Directive {
	
	protected Expr funcNameExpr;
	protected ExprList paraExpr;
	
	protected boolean nullSafe = false;		// 是否支持函数名不存在时跳过
	
	public void setExprList(ExprList exprList) {
		int len = exprList.length();
		if (len == 0) {
			throw new ParseException("Template function name required", location);
		}
		
		int index = 0;
		Expr expr = exprList.getExpr(index);
		if (expr instanceof Const && ((Const)expr).isBoolean()) {
			if (len == 1) {
				throw new ParseException("Template function name required", location);
			}
			
			nullSafe = ((Const)expr).getBoolean();
			index++;
		}
		
		funcNameExpr = exprList.getExpr(index++);
		
		ArrayList<Expr> list = new ArrayList<Expr>();
		for (int i=index; i<len; i++) {
			list.add(exprList.getExpr(i));
		}
		paraExpr = new ExprList(list);
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		Object funcNameValue = funcNameExpr.eval(scope,env);
		if (funcNameValue == null) {
			if (nullSafe) {
				return ;
			}
			throw new TemplateException("Template function name can not be null", location);
		}
		
		if (!(funcNameValue instanceof String)) {
			throw new TemplateException("Template function name must be String", location);
		}
		
		Define func = env.getFunction(funcNameValue.toString());
		
		if (func == null) {
			if (nullSafe) {
				return ;
			}
			throw new TemplateException("Template function not found : " + funcNameValue, location);
		}
		
		func.call(env, scope, paraExpr, writer);
	}
}




