
package use.template;

import use.template.expr.ast.ExprList;
import use.template.stat.ast.Stat;

/**
 * Directive 供用户继承并扩展自定义指令，具体用法可以参考
 * use.template.ext.directive 包下面的例子
 *
 * Directive在一个Template实例中 是多例的, 举例: 某个模板中存在两个 #random() | #random()  那么在初始化, 解析模板时, 就会创建两个random实例
 *
 */
public abstract class Directive extends Stat {

	/**
	 * 传递给指令的表达式列表
	 * 1：表达式列表可通过 exprList.eval(scope) 以及 exprList.evalExprList(scope) 进行求值
	 * 2:使用赋值表达式可实现参数传递功能
	 *
	 * <pre>
	 * 例如：#render("_hot.html", title="热门新闻", list=newsList)
	 * </pre>
	 */
	protected ExprList exprList;

	/**
	 * 具有 #end 结束符的指令内部嵌套的所有内容，调用 stat.exec(env, scope, writer)
	 * 即可执行指令内部嵌入所有指令与表达式，如果指令没有 #end 结束符，该属性无效
	 */
	protected Stat stat;

	/**
	 * 指令被解析时注入指令参数表达式列表，继承类可以通过覆盖此方法对参数长度和参数类型进行校验
	 */
	public void setExprList(ExprList exprList) {
		this.exprList = exprList;
	}

	/**
	 * 指令被解析时注入指令 body 内容，仅对于具有 #end 结束符的指令有效
	 */
	@Override
	public void setStat(Env env,Stat stat) {
		this.stat = stat;
	}
}





