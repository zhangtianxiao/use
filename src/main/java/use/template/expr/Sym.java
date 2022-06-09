
package use.template.expr;

/**
 * Sym
 */
public enum Sym {
	
	ASSIGN("="),
	
	DOT("."), RANGE(".."), COLON(":"), STATIC("::"), COMMA(","), SEMICOLON(";"),
	LPAREN("("), RPAREN(")"), LBRACK("["), RBRACK("]"), LBRACE("{"), RBRACE("}"),
	
	ADD("+"), SUB("-"), INC("++"), DEC("--"),
	MUL("*"), DIV("/"), MOD("%"),
	
	EQUAL("=="), NOTEQUAL("!="), LT("<"), LE("<="), GT(">"), GE(">="), 
	
	NOT("!"), AND("&&"), OR("||"),
	
	QUESTION("?"),
	NULL_SAFE("??"),
	
	ID("ID"),
	
	STR("STR"), TRUE("TRUE"), FALSE("FALSE"), NULL("NULL"),
	INT("INT"), LONG("LONG"), FLOAT("FLOAT"), DOUBLE("DOUBLE"),
	
	EOF("EOF");
	
	private final String value;
	
	private Sym(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
	
	public String toString() {
		return value;
	}
}





