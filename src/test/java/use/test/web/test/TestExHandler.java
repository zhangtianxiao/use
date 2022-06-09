package use.test.web.test;

import org.springframework.stereotype.Component;
import use.mvc.router.Action;
import use.mvc.router.ActionInfo;
import use.mvc.mi.ExceptionHandler;

@Component
public class TestExHandler implements ExceptionHandler {
  @Override
  public boolean match(ActionInfo info, Action action, Throwable e) {
    return true;
  }

  @Override
  public void handle(ActionInfo info, Action action, Throwable e) {
    action.end(500);
  }
}
