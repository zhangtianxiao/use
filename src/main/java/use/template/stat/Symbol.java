
package use.template.stat;

import java.util.HashMap;
import java.util.Map;

/**
 * Symbol
 */
enum Symbol {
	
	TEXT("text", false),
	
	OUTPUT("output", true),
	
	DEFINE("define", true),
	CALL("call", true),
	CALL_IF_DEFINED("callIfDefined", true),
	SET("set", true),
	SET_LOCAL("setLocal", true),
	SET_GLOBAL("setGlobal", true),
	INCLUDE("include", true),
	
	FOR("for", true),
	IF("if", true),
	ELSEIF("elseif", true),
	ELSE("else", false),
	END("end", false),
	CONTINUE("continue", false),
	BREAK("break", false),
	RETURN("return", false),
	
	SWITCH("switch", true),
	CASE("case", true),
	DEFAULT("default", false),
	
	ID("ID", false),				// 标识符：下划线或字母开头 ^[A-Za-z_][A-Za-z0-9_]*$
	PARA("PARA", false),
	
	EOF("EOF", false);
	
	private final String name;
	private final boolean hasPara;	// 是否有参
	
	@SuppressWarnings("serial")
	private static final Map<String, Symbol> keywords = new HashMap<String, Symbol>(64) {{
		put(Symbol.IF.getName(), IF);
		put(Symbol.ELSEIF.getName(), ELSEIF);
		put(Symbol.ELSE.getName(), ELSE);
		put(Symbol.END.getName(), END);
		put(Symbol.FOR.getName(), FOR);
		put(Symbol.BREAK.getName(), BREAK);
		put(Symbol.CONTINUE.getName(), CONTINUE);
		put(Symbol.RETURN.getName(), RETURN);
		
		put(Symbol.SWITCH.getName(), SWITCH);
		put(Symbol.CASE.getName(), CASE);
		put(Symbol.DEFAULT.getName(), DEFAULT);
		
		put(Symbol.DEFINE.getName(), DEFINE);
		put(Symbol.SET.getName(), SET);
		put(Symbol.SET_LOCAL.getName(), SET_LOCAL);
		put(Symbol.SET_GLOBAL.getName(), SET_GLOBAL);
		put(Symbol.INCLUDE.getName(), INCLUDE);
	}};
	
	private Symbol(String name, boolean hasPara) {
		this.name = name;
		this.hasPara = hasPara;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
	
	boolean hasPara() {
		return hasPara;
	}
	
	boolean noPara() {
		return !hasPara;
	}
	
	public static Symbol getKeywordSym(String name) {
		return keywords.get(name);
	}
}




