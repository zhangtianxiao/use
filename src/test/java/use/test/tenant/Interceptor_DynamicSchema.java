package use.test.tenant;

import use.aop.Interceptor;
import use.aop.Invocation;
import use.jdbc.Db;
import use.jdbc.DbKit;
import use.mvc.router.Action;

import static use.kit.Helper.as;

public class Interceptor_DynamicSchema implements Interceptor {
  @Override
  public void intercept(Invocation inv) {
    Action arg = as(inv.args.get(0));
    String host = arg.header("HOST");
    // hostToSchema()
    String  schema = "";
    Db db = DbKit.db(schema);
    DbConfig_DynamicSchema config = as(db.config);
    config.setSchema("");
  }
}
