package use.test.web.test;

import io.vertx.core.Vertx;
import org.springframework.stereotype.Component;
import use.aop.Interceptor;
import use.aop.Invocation;

import javax.annotation.Resource;

@Component
public class TestInterceptor implements Interceptor {
  @Resource
  Vertx vertx;

  @Override
  public void intercept(Invocation inv) {
    System.err.println("invoke...");
    inv.invoke();
  }
}
