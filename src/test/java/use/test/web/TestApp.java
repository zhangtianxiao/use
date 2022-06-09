package use.test.web;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import use.aop.Aop;
import use.hotswap.HotSwap;
import use.kit.BeanHolder;
import use.kit.VertxKit;
import use.test.web.test.TestRouter;

import java.util.function.Supplier;

@Configuration
public class TestApp implements Runnable, AutoCloseable {

  @Bean(destroyMethod = "destroy")
  public BeanHolder<Vertx> vertxBeanHolder() {
    return new BeanHolder<>() {
      final VertxOptions vertxOptions = new VertxOptions().setBlockedThreadCheckInterval(100 * 1000);
      final Vertx vertx = Vertx.vertx(vertxOptions);

      @Override
      public Vertx get() {
        return vertx;
      }

      @Override
      public void destroy() {
        VertxKit.await(vertx.close());
        System.err.println("vertx closed..." + vertx);
      }
    };
  }

  @Bean
  public Vertx vertx(BeanHolder<Vertx> vertxBeanHolder) {
    return vertxBeanHolder.get();
  }

  @Bean
  public TestRouter testRouter(Vertx vertx, ApplicationContext spring) {
    TestRouter router = new TestRouter(vertx);
    router.scan(spring::getBean, "use.test.web");
    return router;
  }


  static ConfigurableApplicationContext spring;

  @Override
  public void close() throws Exception {
    System.out.println("close...");
    spring.close();
  }

  @Override
  public void run() {
    ConfigurableApplicationContext $ = Aop.bySpring("use.test.web");
    spring = $;
    Vertx vertx = $.getBean(Vertx.class);

    DeploymentOptions dp = new DeploymentOptions();
    dp.setInstances(4);

    Supplier<Verticle> supplier = () -> $.getBean(TestVerticle.class);
    VertxKit.await(vertx.deployVerticle(supplier, dp));

    //$.close();
  }

  public static void main(String[] args) {
    HotSwap.create(TestApp.class).addHotSwapClassPrefix("use.test.web").start();
  }
}
