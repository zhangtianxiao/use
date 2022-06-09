

package use.jdbc.graph;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Ref {
  // 待实现 多数据源聚合 需要一个地方接管存放db, 如Global.db("");
  String db() default "";

  String value() default "";

  String[] keys() default {};

  String breakCond() default "";

  boolean lazy() default  false;
}


