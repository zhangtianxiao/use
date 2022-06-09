package use.mvc.router;

import use.aop.Interceptor;
import use.aop.Invocation;
import use.aop.Callback;
import use.mvc.mi.CORS;
import use.mvc.mi.ExceptionHandler;
import use.mvc.mi.HttpMethod;
import use.mvc.mi.MappingInfo;
import use.mvc.pararesolver.ParaResolver;

import java.lang.reflect.Method;

public class ActionInfo {
  public final MappingInfo mi;
  public final Callback dispelCall;
  public final Method method;
  public final Interceptor[] interceptors;
  public final ParaResolver[] paraResolvers;
  public final Object o;
  final ExceptionHandler[] exceptionHandlers;
  public final boolean usePathVariable;
  public final CORS cors;


  public ActionInfo(MappingInfo mi
    , Object o
    , Method method
    , Interceptor[] interceptors
    , ParaResolver[] paraResolvers
    , Callback dispelCall
    , ExceptionHandler[] exceptionHandlers) {
    this.mi = mi;
    this.dispelCall = dispelCall;
    this.method = method;
    this.interceptors = interceptors;
    this.paraResolvers = paraResolvers;
    this.o = o;
    this.usePathVariable = mi.path.contains("/:");
    this.exceptionHandlers = exceptionHandlers;

    CORS CORSConfig = o.getClass().getAnnotation(CORS.class);
    this.cors = CORSConfig != null ? CORSConfig : method.getAnnotation(CORS.class);

    System.err.println("route: " + (mi.websocket ? "websocket" : "http") + " " + mi.method + "  " + mi.path + "  " + method);
  }


  void buildArgs(Action action, Invocation inv) {
    for (int i = 0; i < paraResolvers.length; i++) {
      inv.args.add(paraResolvers[i].resolve(action));
    }
  }

  public boolean isPost() {
    return mi.method == HttpMethod.POST;
  }

  public boolean isGet() {
    return mi.method == HttpMethod.GET;
  }

  public boolean isOptions() {
    return mi.method == HttpMethod.OPTIONS;

  }





/*
  @GET("")
  public String index(Action action) {
  }*/
}
