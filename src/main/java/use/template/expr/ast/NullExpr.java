
package use.template.expr.ast;

import use.template.stat.Scope;

/**
 * NullExpr
 */
public class NullExpr extends Expr {
	
	public static final NullExpr me = new NullExpr();
	
	private NullExpr() {}
	
	public Object eval(Scope scope) {
		return null;
	}
}



