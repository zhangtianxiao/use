package use.test.vertx.httpproxy;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.httpproxy.HttpProxy;

public class Test {
  // https://vertx.io/docs/4.2.0/vertx-http-proxy/java/
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    /*HttpServer originServer = vertx.createHttpServer();
    originServer.requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/html")
        .end("<html><body><h1>I'm the target resource!</h1></body></html>");
    }).listen(7070);*/

    HttpClient proxyClient = vertx.createHttpClient();

    HttpProxy proxy = HttpProxy.reverseProxy(proxyClient);
    proxy.origin(3002, "localhost");

    HttpServer proxyServer = vertx.createHttpServer();

    proxyServer.requestHandler((it) -> {
      String api = it.getHeader("api");
      if (api != null) {
      }
      //  走本地静态资源服务器
      else
        proxy.handle(it);
    }).listen(8080);

  }
}
