package use.test.vertx.spring;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import use.test.web.test.TestRouter;

@SpringBootApplication(scanBasePackages = {"use.test.web.test", "use.test.vertx.spring"})
/*@ComponentScans({
  @ComponentScan("use.test.web")
  , @ComponentScan("use.test.vertx.spring")
})*/
public class App {
  public static final Vertx vertx = Vertx.vertx(new VertxOptions().setBlockedThreadCheckInterval(100 * 1000));

  @Bean
  public Vertx vertx() {
    return vertx;
  }

  @Bean
  public TestRouter testRouter(ApplicationContext $) {
    TestRouter router = new TestRouter(vertx);
    router.scan($::getBean, "use.test.web.test");
    return router;
  }
}
