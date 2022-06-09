
package use.template.stat;

/**
 * Token
 */
class Token {
	
	final Symbol symbol;
	final int row;
	private final String value;
	
	Token(Symbol symbol, String value, int row) {
		if (symbol == null || value == null) {
			throw new IllegalArgumentException("symbol and value can not be null");
		}
		this.symbol = symbol;
		this.value = value;
		this.row = row;
	}
	
	Token(Symbol symbol, int row) {
		this(symbol, symbol.getName(), row);
	}
	
	boolean hasPara() {
		return symbol.hasPara();
	}
	
	boolean noPara() {
		return symbol.noPara();
	}
	
	public String value() {
		return value;
	}
	
	public String toString() {
		return value;
	}
	
	public int getRow() {
		return row;
	}
	
	public void print() {
		System.out.print("[");
		System.out.print(row);
		System.out.print(", ");
		System.out.print(symbol.getName());
		System.out.print(", ");
		System.out.print(value());
		System.out.println("]");
	}
}


