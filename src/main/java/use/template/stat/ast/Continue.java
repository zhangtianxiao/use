
package use.template.stat.ast;

import use.template.io.Writer;
import use.template.Env;
import use.template.stat.Scope;

/**
 * Continue
 */
public class Continue extends Stat {
	
	public static final Continue me = new Continue();
	
	private Continue() {
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		scope.getCtrl().setContinue();
	}
}




