
package use.template.stat.ast;

import use.template.io.Writer;
import use.template.Env;
import use.template.stat.Scope;

/**
 * Default
 * 
 * #switch 指令内部的 #default 指令
 */
public class Default extends Stat {
	
	private Stat stat;
	
	public Default(StatList statList) {
		this.stat = statList.getActualStat();
	}
	
	public void exec(Env env, Scope scope, Writer writer) {
		stat.exec(env, scope, writer);
	}
}