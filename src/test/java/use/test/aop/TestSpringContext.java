package use.test.aop;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import use.aop.Aop;
import use.kit.Helper;

public class TestSpringContext {
  public static void main(String[] args) {
    TimeInterval timer = DateUtil.timer();
    timer.start();
    var spring = Aop.bySpring("use.test.aop");

    var c3 = spring.getBean(TestService.class);
    var c4 = spring.getBean(TestService.class);
    Helper.Assert(c3 != c4);
    System.out.println(c3.query());
    System.out.println(c3.query2("1", "2"));
    System.out.println(timer.intervalPretty());
  }

}
