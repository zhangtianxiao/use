
package use.template.expr;

/**
 * Tok
 */
class Tok {
	
	final Sym sym;
	private final String value;
	final int row;
	
	Tok(Sym sym, int row) {
		this(sym, sym.value(), row);
	}
	
	Tok(Sym exprSym, String value, int row) {
		if (exprSym == null || value == null) {
			throw new IllegalArgumentException("exprSym and value can not be null");
		}
		this.sym = exprSym;
		this.value = value;
		this.row = row;
	}
	
	String value() {
		return value;
	}
	
	public String toString() {
		return value;
	}
	
	void print() {
		System.out.print("[");
		System.out.print(row);
		System.out.print(", ");
		System.out.print(sym.value());
		System.out.print(", ");
		System.out.print(value());
		System.out.println("]");
	}
}





