package use.mvc.mi;

import use.mvc.router.Action;
import use.mvc.router.ActionInfo;

public interface ExceptionHandler {
  boolean match(ActionInfo info, Action action, Throwable e);
  void handle(ActionInfo info, Action action, Throwable e);
}
