
package use.aop;

import java.lang.annotation.*;

/**
 * Clear is used to clear all interceptors or the specified interceptors,
 * it can not clear the interceptor which declare on method.
 * 
 * <pre>
 * Example:
 * 1: clear all interceptors but InterA and InterB will be kept, because InterA and InterB declare on method
 * @Clear
 * @Before({InterA.class, InterB.class})
 * public void method(...)
 * 
 * 2: clear InterA and InterB, InterC and InterD will be kept
 * @Clear({InterA.class, InterB.class})
 * @Before({InterC.class, InterD.class})
 * public void method(...)
 * </pre>
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD})
public @interface Clear {
	Class<? extends Interceptor>[] value() default {};
}

