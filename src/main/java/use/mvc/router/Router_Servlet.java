package use.mvc.router;

import cn.hutool.extra.servlet.ServletUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import use.aop.Invocation;
import use.mvc.mi.ExceptionHandler;
import use.mvc.mi.MappingInfo;

import javax.servlet.GenericServlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

import static use.kit.Helper.as;

public class Router_Servlet extends Router {
  public static final Logger logger = LoggerFactory.getLogger(Router_Servlet.class);

  public ActionRouter router = ActionRouter.create();

  public final GenericServlet Servlet = new GenericServlet() {
    @Override
    public void service(ServletRequest req, ServletResponse res) {
      router.doHandle(new Action_Servlet(req, res));
    }
  };

  @Override
  public void route(ActionInfo info) {
    //UploadSetting uploadSetting = new UploadSetting();
    router.route(info.mi.method.name(), info.mi.path, h -> {
      Action_Servlet action = as(h);
      HttpServletRequest request = action.request;
      MappingInfo mi = info.mi;
      if (info.isPost()) {
        if (mi.max != -1 && request.getContentLengthLong() > mi.max) {
          action.endWithStatus(504);
          return;
        } else if (mi.multipart) {
          action.formData = ServletUtil.getMultipart(request);
        } else if (mi.form_urlencoded) {
          // 触发servlet容器对body的解析, 避免后续对body的读取(如有),导致容器忽略参数解析
          request.getParameterNames();
        } else {
          int len = request.getContentLength();
          ByteBuffer buf = ByteBuffer.allocate(len);
          byte[] array = buf.array();
          try {
            request.getInputStream().read(array);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
          action.payload = buf;
        }
      }
      call(info, as(h));
    });
  }

  private void call(ActionInfo info, Action_Servlet action) {
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
            HttpServletResponse response = action.response;
            response.setStatus(406);
            logger.error("return value of websocket action must be WebSocketSession.class");
            return;
          }
          action.status(101);
          //action.end();
          /*request.pause();
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
          });*/
        }
      } else {
        if (action.ended()) {
        } else {
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
