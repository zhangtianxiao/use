package use.mvc.mi;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Mapping {
  String value();

  HttpMethod method() default HttpMethod.GET;

  boolean websocket() default false;

  boolean async() default false;

  long timeout() default -1;

  long max() default 1024 * 1024 * 100;

  boolean multipart() default false;

  boolean form_urlencoded() default false;

  boolean to_disk() default false;

  Class<? extends ExceptionHandler>[] onError() default {};
}
