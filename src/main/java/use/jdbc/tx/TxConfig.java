
package use.jdbc.tx;

import java.lang.annotation.*;

/**
 * TxConfig is used to configure configName for Tx interceptor
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TxConfig {
	String value();		// configName of Config
}




