package use.test.hotswap;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import use.aop.Aop;
import use.hotswap.HotSwap;
import use.test.aop.TestService;

import java.lang.reflect.Method;

/**
 项目启动示例 */
@ComponentScan("use.test")
public class StartSpringboot implements Runnable, AutoCloseable {

  public static void main(String[] args) {
    HotSwap.create(StartSpringboot.class)
      .addHotSwapClassPrefix("use.test")
      .start();
  }

  ConfigurableApplicationContext spring;

  @Override
  public void run() {
    System.gc();

    TestService service = new TestService("service");
    System.out.println(service);
    System.out.println(service.getClass());
    System.out.println(service.getClass().getClassLoader());
    System.out.println(service.getClass().getClassLoader().getParent());
    System.out.println(service.query());

    spring = Aop.bySpringBoot(StartSpringboot.class);
    TestService bean = spring.getBean("testService", TestService.class);
    for (Method declaredMethod : bean.getClass().getDeclaredMethods()) {
      System.out.println(declaredMethod);
    }

    System.gc();
  }

  @Override
  public void close() {
    if (spring != null)
      spring.stop();
  }
}

