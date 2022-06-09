package use.test.bean.circular;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import use.kit.ex.Unsupported;

public class circularTest {
  public static void main(String[] args) {
    var spring = new AnnotationConfigApplicationContext();
    spring.scan("use.test.bean.circular");
    spring.refresh();

    A a = spring.getBean(A.class);
    B b = spring.getBean(B.class);
    eq(a.b == b);
    eq(b.a == a);
  }

  static void eq(boolean b) {
    if (!b)
      throw new Unsupported();
  }
}
