
package use.template;

import use.template.stat.Location;

/**
 * Template runtime exception
 */
@SuppressWarnings("serial")
public class TemplateException extends RuntimeException {
	
	public TemplateException(String msg, Location loc) {
		super(loc != null ? msg + loc : msg);
	}
	
	public TemplateException(String msg, Location loc, Throwable cause) {
		super(loc != null ? msg + loc : msg, cause);
	}
}


