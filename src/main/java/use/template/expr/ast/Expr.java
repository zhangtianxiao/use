
package use.template.expr.ast;

import use.template.stat.Location;
import use.template.stat.Scope;

/**
 * Expr
 */
public abstract class Expr {
	
	protected Location location;
	
	public abstract Object eval(Scope scope);
}




