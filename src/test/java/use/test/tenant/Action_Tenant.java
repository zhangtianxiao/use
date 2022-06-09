package use.test.tenant;

import io.vertx.ext.web.RoutingContext;
import use.jdbc.Db;
import use.jdbc.DbKit;
import use.mvc.router.Action_Vertx;

import static use.kit.Helper.as;

public class Action_Tenant extends Action_Vertx {
  private Db db;

  public Action_Tenant(RoutingContext ctx) {
    super(ctx);
    // tenantId;
  }

  public Db db() {
    return db("");
  }

  public Db db(String name) {
    if (db == null) {
      db = DbKit.db(name);
      DbConfig_DynamicSchema config = as(db.config);
      config.setSchema("");
    }
    return db;
  }
}
