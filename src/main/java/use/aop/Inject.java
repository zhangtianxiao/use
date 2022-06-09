package use.aop;

import java.lang.annotation.*;

/**
 * Inject is used to inject dependent object
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Inject {
	Class<?> value() default Void.class;					// 被注入类的类型
}

