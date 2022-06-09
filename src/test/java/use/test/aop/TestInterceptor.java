package use.test.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import use.aop.Interceptor;
import use.aop.Invocation;

@Component
public class TestInterceptor implements Interceptor {
  public static final Logger log = LoggerFactory.getLogger(TestInterceptor.class);

  @Override
  public void intercept(Invocation inv) {
    log.info("before");
    log.info(inv.getMethod().toString());
    inv.invoke();
    log.info("after");
  }
}
