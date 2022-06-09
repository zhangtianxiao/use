
package use.template.stat.ast;

import use.template.io.Writer;
import use.template.Env;
import use.template.stat.Scope;

/**
 * NullStat
 */
public class NullStat extends Stat {
	
	public static final NullStat me = new NullStat();
	
	private NullStat() {}
	
	public void exec(Env env, Scope scope, Writer writer) {
		
	}
}





