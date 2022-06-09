package use.mvc.mi;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface GET {
  String value();

  boolean websocket() default false;

  boolean async() default false;

  long timeout() default -1;

  Class<? extends ExceptionHandler>[] onError() default {};
}
