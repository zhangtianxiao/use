package use.test.tenant;

import use.jdbc.Db;
import use.mvc.router.Action;

public class SwitchSchema {
  static final ThreadLocal<String> TL = new ThreadLocal<>();

  public void doSwitch(Action action, Db db){

    String host = action.header("host");
  }
}
