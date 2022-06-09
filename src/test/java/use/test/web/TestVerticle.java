package use.test.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import use.test.web.test.TestRouter;

@Component
@Scope("prototype")
public class TestVerticle extends AbstractVerticle {
  final TestRouter router;

  public TestVerticle(TestRouter router) {
    this.router = router;
  }

  @Override
  public void start(Promise ok) throws Exception {
    HttpServer server = vertx.createHttpServer();
    server.requestHandler(router.handler).listen(8081).onComplete(ok);
  }

}
