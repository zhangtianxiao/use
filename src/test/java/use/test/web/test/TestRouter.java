package use.test.web.test;

import io.netty.buffer.ByteBuf;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import use.aop.Interceptor;
import use.kit.Helper;
import use.kit.NettyKit;
import use.kit.NettyWriter;
import use.kit.VertxKit;
import use.aop.Callback;
import use.mvc.mi.ExceptionHandler;
import use.mvc.mi.MappingInfo;
import use.mvc.pararesolver.ParaResolver;
import use.mvc.router.Action;
import use.mvc.router.ActionInfo;
import use.mvc.router.Action_Vertx;
import use.mvc.router.Router_Vertx;
import use.template.Template;

import java.lang.reflect.Method;
import java.util.Map;

import static use.kit.Helper.as;

public class TestRouter extends Router_Vertx {
  public TestRouter(Vertx vertx) {
    super(vertx);
  }

  @Override
  protected ActionInfo createActionInfo(MappingInfo mi, Object o, Method method, Interceptor[] interceptors, ParaResolver[] paraResolvers, Callback dispelCall, ExceptionHandler[] exceptionHandlers) {
    return new TestActionInfo(mi, o, method,interceptors, paraResolvers, dispelCall, exceptionHandlers);
  }

  @Override
  public boolean beforeParaResolve(Action action, ActionInfo actionInfo) {
    TestActionInfo act = as(actionInfo);
    act.total_access++;
    return true;
  }

  @Override
  public boolean afterParaResolve(Action action, ActionInfo actionInfo) {
    TestActionInfo act = as(actionInfo);
    act.valid_access++;
    return true;
  }

  @Override
  protected void handleResult(Action action, Object o) {
    Action_Vertx vAction = as(action);
    if (o instanceof Template template) {
      Map<String, Object> data = vAction.ctx.data();
      NettyWriter writer = new NettyWriter();
      try {
        template.render(data, writer);
        vAction.ctx.end(Buffer.buffer(writer.buffer()));
      } catch (Throwable e) {
        writer.buffer().release();
        throw Helper.asRuntimeException(e);
      } finally {
        writer.close();
      }
    } else if (o instanceof NettyWriter writer) {
      vAction.ctx.end(Buffer.buffer(writer.buffer()));
      //action.end(writer.buffer().nioBuffer());
    } else if (o instanceof CharSequence chars) {
      vAction.ctx.end(VertxKit.asBuffer(chars));
    } else {
      ByteBuf byteBuf = NettyKit.writeJson(o);
      vAction.ctx.end(Buffer.buffer(byteBuf));
    }
  }
}
