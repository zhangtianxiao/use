package use.test.web.test;

import io.vertx.core.Vertx;
import org.springframework.stereotype.Component;
import use.aop.Interceptor;
import use.aop.Invocation;

import javax.annotation.Resource;

@Component
public class AsyncInterceptor implements Interceptor {
  @Resource
  Vertx vertx;

  @Override
  public void intercept(Invocation inv) {
    vertx.setTimer(1000, id -> {
      System.err.println("async invoke...");
      inv.invoke();
    });
  }
}
