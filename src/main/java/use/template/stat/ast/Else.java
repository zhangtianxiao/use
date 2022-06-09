
package use.template.stat.ast;

import use.template.io.Writer;
import use.template.Env;
import use.template.stat.Scope;

/**
 * Else
 */
public class Else extends Stat {
	
	private Stat stat;
	
	public Else(StatList statList) {
		this.stat = statList.getActualStat();
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		stat.exec(env, scope, writer);
	}
}



