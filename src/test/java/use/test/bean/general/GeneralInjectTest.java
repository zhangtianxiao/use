package use.test.bean.general;

import use.aop.Aop;
import use.kit.ex.Unsupported;

public class GeneralInjectTest {
  public static void main(String[] args) {
    var spring = Aop.bySpring("use.test.bean.general");
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
