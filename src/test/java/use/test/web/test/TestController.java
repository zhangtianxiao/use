package use.test.web.test;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.URLUtil;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import use.aop.Aop;
import use.aop.Before;
import use.kit.Helper;
import use.kit.Kv;
import use.mvc.mi.*;
import use.mvc.parabind.*;
import use.mvc.router.Action;
import use.mvc.router.Router;
import use.mvc.router.WebSocketSession;
import use.test.web.TestVerticle;

import javax.annotation.Resource;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Supplier;

import static use.kit.Helper.as;

@Component
@Before(TestInterceptor.class)
public class TestController {
  @Resource
  Router router;
  @Resource
  Vertx vertx;

  //public TestController(TestRouter router) {
  public TestController() {
    this.router = router;
  }

  // http://localhost:8081/?id=777
  @Mapping("/")
  public void index(Action action, Integer id, @HeaderValue String host, @CookieValue String sessionId) {
    StringBuilder sb = new StringBuilder();
    sb.append("index, id: ").append(id).append("\n");
    sb.append("host: ").append(host).append("\n");
    sb.append("cookie: ").append(sessionId).append("\n");
    if (sessionId == null) action.cookie("sessionId", 12345678, true);
    action.end(sb);
  }

  // http://localhost:8081/test.html
  @Mapping("/test.html")
  public void test(Action action) {
    action.sendHtml("file-test/test.html");
  }

  // console.log(encodeURIComponent(JSON.stringify({"name":"天下3%ok"})))
  // http://localhost:8081/qs?%7B%22name%22%3A%22%E5%A4%A9%E4%B8%8B3%25ok%22%7D
  @Mapping("/qs")
  public void qs(Action action) {
    action.end(URLUtil.decode(action.qs()));
  }

  @POST(value = "/query", form_urlencoded = true)
  public void query(Action action) {
    Map<String, ?> map = Kv.create().set("id", action.params("id")).set("name", action.param("name"));
    action.end(map);
  }

  // http://localhost:8081/user/777
  @GET("/user/:id")
  public void userHome(@PathVariable Long id, Action action) {
    action.end("index, id: " + id);
  }

  // http://localhost:8081/user/777/hh
  @GET("/user/:id/:module")
  public void userModuleHome(@PathVariable Long id, @PathVariable String module, Action action) {
    action.end("index, id: " + id + " module: " + module);
  }

  /**
   fetch('http://localhost:8081/request_body',{method:'POST',body:'1'.repeat(1024)}).then(it=>it.text()).then(console.log);;
   */
  @POST("/request_body")
  public void request_body(Action action, ByteBuffer buf, byte[] bytes, ByteBufferBackedInputStream input, @RequestBody String body) {
    if (bytes.length == 1024 && body.length() == 1024 && buf.remaining() == 1024 && input.available() == 1024) action.end(buf);
    else action.send("body handed error");
  }

  /**
   fetch('http://localhost:8081/request_body_as_file',{method:'POST',body:'1'.repeat(1024)}).then(it=>it.text()).then(console.log);;
   */
  @POST(value = "/request_body_as_file", to_disk = true)
  public void request_body_as_file(Action action, File file) {
    action.send(file);
  }

  /**
   var file = new File(['Hello', '\n World'], 'hello-world.txt', {type: 'text/plain'})
   var fd = new FormData();
   fd.append('file',file);
   fetch('http://127.0.0.1:8081/upload',{method:'POST',body:fd}).then(it=>it.text()).then(console.log);
   */
  @POST(value = "/upload", multipart = true)
  public void upload(Action action, UploadedFile file) {
    if (file.size() == 12) if (file.file == null) action.end("upload: " + file.temp);
    else action.end("upload: " + file.file.getAbsolutePath());
    else action.end("upload failed");
  }

  @OPTIONS("/cors")
  @CORS
  public void cors_options(Action action) {
    action.end();
  }

  @POST("/cors")
  public void cors_post(Action action) {
    System.out.println("sleep");
    ThreadUtil.sleep(5000);
    action.end("sleep");
  }

  @GET(value = "/websocket.html")
  public void websocket(Action action) {
    action.send("websocket.html");
  }

  @GET(value = "/ws", websocket = true)
  public WebSocketSession upgrade(Action action) {
    return new TestWebSocketSession();
  }

  @GET("/sleep")
  public void sleep(Action action) {
    System.out.println("sleep");
    ThreadUtil.sleep(5000);
    action.end("sleep");
  }

  @Before(AsyncInterceptor.class)
  @GET(value = "/async", async = true)
  public void async(Action action) {
    System.err.println("async");
    vertx.setTimer(2000, tid -> {
      action.end("async task done");
    });
  }


  @GET(value = "/error", onError = {TestExHandler.class})
  public void error(Action action) {
    System.out.println(1 / 0);
  }

  @GET("/router")
  public Object router() {
    StringBuilder sb = new StringBuilder(1024);
    for (int i = 0; i < router.actionInfos.size(); i++) {
      TestActionInfo it = as(router.actionInfos.get(i));
      sb.append(i + 1).append(". ").append(it.mi.method).append(" ").append(it.mi.path).append("\n");
      Class<?> a = it.o.getClass();
      while (a.getName().contains("$$")) a = a.getSuperclass();
      sb.append("class:  ").append(a).append("\n");
      sb.append("method: ").append(it.method).append("\n");
      sb.append("total_access: ").append(it.total_access).append("\n");
      sb.append("valid_access: ").append(it.valid_access).append("\n");
    }
    return sb;
  }


  @GET("/jvminfo")
  public StringBuilder jvminfo(Action action) {
    action.header("Content-Type", "text/plain");
    return Helper.jvminfo().append("context: ").append(Vertx.currentContext()).append("\n");
  }


  public static void main(String[] args) {
    ConfigurableApplicationContext spring = Aop.bySpring("use.test.web");

    VertxOptions vertxOptions = new VertxOptions().setBlockedThreadCheckInterval(100 * 1000);
    Vertx vertx = Vertx.vertx(vertxOptions);

    TestRouter router = new TestRouter(vertx);
    router.scan(spring::getBean, "use.test.web");

    DeploymentOptions dp = new DeploymentOptions();
    dp.setInstances(4);
    Supplier<Verticle> supplier = () -> spring.getBean(TestVerticle.class);
/*
    Supplier<Verticle> supplier = () -> new AbstractVerticle() {
      @Override
      public void start(Promise ok) throws Exception {
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router.handler).listen(8081).onComplete(ok);
      }
    };
*/
    vertx.deployVerticle(supplier, dp);
  }
  // --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true
// -XX:MaxMetaspaceSize=30M -XX:MaxDirectMemorySize=10M -Xms50M -Xmx50M --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true

  // dispel
  /*public static void main(String[] f) {
    BiFunction<Object,Object[], Object> call = (o,args)->{
      TestController it = (TestController)o;
      it.test((Action) args[0]);
      return null;
    };
  }*/
}
