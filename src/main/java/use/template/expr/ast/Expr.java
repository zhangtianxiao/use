
package use.template.expr.ast;

import use.template.EngineConfig;
import use.template.Env;
import use.template.stat.Location;
import use.template.stat.Scope;

/**
 * Expr
 */
public abstract class Expr {
	
	protected Location location;
	
	public abstract Object eval(Scope scope, Env env);
}




