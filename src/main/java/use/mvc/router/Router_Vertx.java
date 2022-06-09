package use.mvc.router;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import use.aop.Invocation;
import use.kit.ex.Unsupported;
import use.mvc.mi.ExceptionHandler;
import use.mvc.mi.MappingInfo;
import use.mvc.util.VertxBodyToFileHandler;

import static use.kit.Helper.as;

public class Router_Vertx extends Router {
  public static final Logger logger = LoggerFactory.getLogger(Router_Vertx.class);

  io.vertx.ext.web.Router router;
  final Vertx vertx;
  public final transient Handler<HttpServerRequest> handler = it -> {
    this.router.handle(it);
  };

  public Router_Vertx(Vertx vertx) {
    this.router = io.vertx.ext.web.Router.router(vertx);
    this.vertx = vertx;
  }

  public void replaceRouter(io.vertx.ext.web.Router router, Vertx vertx) {
    this.router = router;
    if (vertx != this.vertx) {
      throw new Unsupported();
    }
  }

  protected Action_Vertx createAction(RoutingContext it) {
    return new Action_Vertx(it);
  }

  @Override
  public void route(ActionInfo info) {
    MappingInfo mi = info.mi;
    Route route = router.route(HttpMethod.valueOf(mi.method.name()), mi.path);
    if (info.isPost()) {
      if (mi.to_disk) {
        route.handler(new VertxBodyToFileHandler(mi.max));
      } else {
        route.handler(BodyHandler.create(mi.multipart).setBodyLimit(mi.max));
      }
      route.handler(it -> call(info, it));
    } else if (info.isGet()) {
      route.handler(it -> call(info, it));
    } else if (info.isOptions()) {
      route.handler(it -> call(info, it));
    }
  }


  private void call(ActionInfo info, RoutingContext it) {
    Action_Vertx action = createAction(it);
    if (info.mi.async && info.mi.timeout != -1) {
      long tid = vertx.setTimer(info.mi.timeout, ignored -> {
        Invocation inv = action.inv();
        if (inv != null)
          inv.canceled = true;
        it.fail(504);
      });
      it.addBodyEndHandler(v -> vertx.cancelTimer(tid));
    }
    try {
      if (!beforeParaResolve(action, info))
        return;

      Invocation inv = Invocation.pool.get();

      // 构建参数
      info.buildArgs(action, inv);
      if (!afterParaResolve(action, info))
        return;

      // 处理跨域
      if (info.cors != null)
        doConfigCORS(action, info.cors);

      // 将inv对象挂进action
      action.setInv(inv);
      inv.init(info.o, info.method, info.interceptors, null);

      // 无反射调用
      Object result = info.dispelCall.invoke(inv);

      if (info.mi.websocket) {
        // websocket handshake failed
        if (result != null) {
          if (result instanceof WebSocketSession) {
          } else {
            HttpServerResponse response = action.ctx.response();
            if (!response.ended()) {
              response.setStatusCode(406);
              response.end();
            }
            logger.error("return value of websocket action must be WebSocketSession.class");
            return;
          }
          action.status(101);
          //action.end();
          HttpServerRequest request = action.ctx.request();
          request.pause();
          request.toWebSocket().onComplete(ar -> {
            if (ar.succeeded()) {
              request.resume();
              ServerWebSocket client = ar.result();
              WebSocketSession ws = as(result);
              ws.setDelegate(new WebSocketSession_Vertx(client));

              client.closeHandler(v -> ws.onClosed());
              client.binaryMessageHandler(v -> ws.onBinary(v.getByteBuf().nioBuffer()));
              client.textMessageHandler(v -> ws.onText(v));
              client.exceptionHandler(v -> ws.onError(v));
            } else {
              logger.error("upgrade websocket by vert.x", ar.cause());
            }
          });
        }
      } else {
        /*if (info.async) {
        }
        else if (!action.ended()) {
          action.end(result);
        }
        else {
          // 不是异步, 也没调用完毕
          action.endWithStatus(503);
        }*/
        if (action.ended()) {
        } else {
          // 调用已结束
          if (inv.handled()) {
            action.end(result);
          }
          // 如果是异步, 等用户自己调用action.end
          else if (info.mi.async) {
          }
          // 不是异步, 也没调用完毕, 1.拦截器拦掉了 2.或没有调用end
          else {
            action.end(result);
          }
        }
      }
    } catch (Throwable e) {
      boolean match = false;
      for (ExceptionHandler exh : info.exceptionHandlers) {
        if (exh.match(info, action, e)) {
          match = true;
          exh.handle(info, action, e);
        }
      }
      if (!match)
        whenError(e);
    } finally {
      if (action.ended()) {
        Invocation inv = action.inv();
        if (inv != null)
          Invocation.pool.recycle(inv);
      }
    }
  }
}