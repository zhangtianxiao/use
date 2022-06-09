
package use.aop;

import java.lang.annotation.*;

/**
 * Before is used to configure Interceptor and Validator.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Before {
	Class<? extends Interceptor>[] value();
}


