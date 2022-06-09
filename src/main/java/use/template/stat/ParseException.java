
package use.template.stat;

/**
 * ParseException
 * 词法、语法错误
 */
@SuppressWarnings("serial")
public class ParseException extends RuntimeException {
	
	public ParseException(String msg, Location loc) {
		super(loc != null ? msg + loc : msg);
	}
	
	public ParseException(String msg, Location loc, Throwable t) {
		super(loc != null ? msg + loc : msg, t);
	}
}

