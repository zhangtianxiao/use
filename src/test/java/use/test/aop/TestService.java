package use.test.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import use.aop.Before;

@Component
@Before(TestInterceptor.class)
@Scope("prototype")
public class TestService {
  public static final Logger log = LoggerFactory.getLogger(TestService.class);
  final String val;

  public TestService(@Value("${test.val}") String val) {
    this.val = val;
  }

  public String query() {
    return val;
  }

  public String query2(String... args) {
    for (String arg : args) {
      log.info(arg);
    }
    return val;
  }


}
