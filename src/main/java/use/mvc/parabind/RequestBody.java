package use.mvc.parabind;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.PARAMETER})
public @interface RequestBody {
  public static String AS_FILE = "RequestBody_AS_FILE";
  public static String AS_ASYNC_FILE = "RequestBody_AS_ASYNC_FILE";
}

