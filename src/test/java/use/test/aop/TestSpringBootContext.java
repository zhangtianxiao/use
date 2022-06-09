package use.test.aop;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import org.springframework.context.annotation.ComponentScan;
import use.aop.Aop;

@ComponentScan("use.test.aop")
public class TestSpringBootContext {
  public static void main(String[] args) {
    TimeInterval timer = DateUtil.timer();
    timer.start();

    var spring = Aop.bySpringBoot(TestSpringBootContext.class);

    var c3 = spring.getBean(TestService.class);
    var c4 = spring.getBean(TestService.class);
    System.out.println(c3 == c4);
    System.out.println(c3.query());
    System.out.println(timer.intervalPretty());
  }
}
