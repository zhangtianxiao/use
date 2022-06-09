
package use.aop;

/**
 * PrototypeInterceptor.
 */
public abstract class PrototypeInterceptor implements Interceptor {
	
	final public void intercept(Invocation inv) {
		try {
			getClass().newInstance().doIntercept(inv);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	abstract public void doIntercept(Invocation inv);
}
