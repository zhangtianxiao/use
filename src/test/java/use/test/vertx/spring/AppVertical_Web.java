package use.test.vertx.spring;

import com.jsoniter.any.Any;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import org.springframework.context.ConfigurableApplicationContext;
import use.aop.Aop;
import use.jdbc.Db;
import use.jdbc.DbKit;
import use.kit.Helper;
import use.kit.VertxKit;
import use.test.web.test.TestRouter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.function.Supplier;

public class AppVertical_Web extends AbstractVerticle {
  static final Supplier<Verticle> maker = AppVertical_Web::new;
  static final Any dbOptions = Helper.ENV.get("dataSourceList").get(1);

  public AppVertical_Web() {
  }


  @Override
  public void start(Promise ok) throws Exception {
    //
    final Db db;
    final ConfigurableApplicationContext $;

    // 多个vertical之间复用数据库连接池
    db = DbKit.newDb(dbOptions);
    VertxKit.put(Db.class, db);

    $ = Aop.bySpringBoot(App.class);
    VertxKit.put("$", $);


    System.out.printf("db: %s spring: %s\n", db, $);
    db.tx(() -> {
      Connection conn1 = db.getConn();
      Connection conn2 = db.getConn();
      Helper.Asset(conn1 == conn2, "相同的连接");
      return true;
    });
    Helper.Asset(db.getConnInHolder() == null, "无连接持有");

    TestRouter router = $.getBean(TestRouter.class);

    HttpServer server = vertx.createHttpServer();
    server.requestHandler(router.handler).listen(8081).onComplete(ok);
  }

  public static void main(String[] args) {
    Vertx vertx = App.vertx;

    DeploymentOptions dp = new DeploymentOptions();
    dp.setInstances(3);

    VertxKit.await(vertx.deployVerticle(maker, dp));
    //VertxKit.await(vertx.deployVerticle(maker, dp));

    //VertxKit.await(vertx.deployVerticle(AppVertical::new, dp));
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      VertxKit.await(vertx.close());
    }));
  }
}
