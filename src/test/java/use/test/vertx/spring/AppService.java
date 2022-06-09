package use.test.vertx.spring;

import io.vertx.core.Vertx;
import org.springframework.stereotype.Component;
import use.jdbc.Db;
import use.kit.VertxKit;

import static use.kit.VertxKit.get;

@Component
public class AppService {
  final Db db = get(Db.class);
  final Vertx vertx = VertxKit.current();

  public void fn() {
    vertx.runOnContext((ig)->{
    })  ;
  }
}
