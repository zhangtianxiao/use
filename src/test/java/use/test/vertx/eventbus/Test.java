package use.test.vertx.eventbus;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

public class Test {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    EventBus eventBus = vertx.eventBus();
  }
}
