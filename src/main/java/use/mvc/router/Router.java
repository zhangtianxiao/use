package use.mvc.router;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import org.jetbrains.annotations.Nullable;
import use.aop.Interceptor;
import use.aop.proxy.DefaultProxyClassFactory;
import use.kit.BeanFactory;
import use.kit.ReflectKit;
import use.kit.StrKit;
import use.kit.ex.Unsupported;
import use.aop.Callback;
import use.mvc.mi.CORS;
import use.mvc.mi.ExceptionHandler;
import use.mvc.mi.MappingInfo;
import use.mvc.pararesolver.ParaResolver;
import use.mvc.pararesolver.ParaResolverDefaults;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class Router {
  public final List<ActionInfo> actionInfos = new ArrayList<>();

  /**
   在参数注入之前做点什么
   比如计数器
   */
  public boolean beforeParaResolve(Action action, ActionInfo actionInfo) {
    return true;
  }

  /**
   参数注入之后做点什么
   比如计数器
   */
  public boolean afterParaResolve(Action action, ActionInfo actionInfo) {
    return true;
  }

  public abstract void route(ActionInfo info);

  protected void handleResult(Action action, Object o) {
    if (o != null)
      action.end("not handled");
  }

  protected void whenError(Throwable e) {
    e.printStackTrace();
  }

  public void scan(BeanFactory beanFactory, String... packageNames) {
    for (String packageName : packageNames) {
      Set<Class<?>> classes = ClassUtil.scanPackage(packageName, it -> !it.isAnonymousClass());
      for (Class<?> c : classes) {
        Method[] methods = ReflectUtil.getMethods(c);
        for (Method method : methods) {
          ActionInfo info = buildActionInfo(beanFactory, c, method);
          if (info != null) {
            route(info);
            actionInfos.add(info);
          }
        }
      }
    }
  }

  /**
   子类重写该方法
   */
  protected ParaResolver[] buildParaResolvers(Parameter[] parameters) {
    return buildParaResolvers(parameters, ParaResolverDefaults.paraResolvers);
  }

  ParaResolver[] buildParaResolvers(Parameter[] parameters, ParaResolver[] qualifiers) {
    ParaResolver[] paraResolvers = new ParaResolver[parameters.length];
    // 默认策略
    for (int i = 0; i < parameters.length; i++) {
      Parameter it = parameters[i];
      Type type = it.getParameterizedType();
      for (ParaResolver paraResolver : qualifiers) {
        if (paraResolver.match(it, type, i)) {
          paraResolvers[i] = paraResolver.resolve(it, type, i);
          break;
        }
      }
    }
    return paraResolvers;
  }

  protected ActionInfo createActionInfo(
    MappingInfo mi
    , Object o
    , Method method
    , Interceptor[] interceptors
    , ParaResolver[] paraResolvers
    , Callback dispelCall
    , ExceptionHandler[] exceptionHandlers) {
    return new ActionInfo(mi, o, method, interceptors, paraResolvers, dispelCall, exceptionHandlers);
  }

  @Nullable
  public ActionInfo buildActionInfo(BeanFactory beanFactory, Class<?> c, Method method) {
    MappingInfo mi = MappingInfo.from(method);
    if (mi == null)
      return null;
    if (mi.path.isEmpty())
      throw new Unsupported();

    Object o1 = beanFactory.get(c);
    Callback call = (Callback) ReflectUtil.getStaticFieldValue(ReflectUtil.getField(o1.getClass(), "CallDispel_" + ReflectKit.hash(method)));
    Objects.requireNonNull(call, "未发现CallDispel, " + method);
    // call = (it, paras) -> ReflectKit.invoke(it, method, paras);
    ParaResolver[] paraResolvers = this.buildParaResolvers(method.getParameters());
    Class<? extends ExceptionHandler>[] onError = mi.onError;
    ExceptionHandler[] exceptionHandlerBeans = new ExceptionHandler[onError.length];
    for (int i = 0; i < onError.length; i++) {
      Class<? extends ExceptionHandler> it = onError[i];
      exceptionHandlerBeans[i] = beanFactory.get(it);
    }
    Object o = beanFactory.get(c);
    Interceptor[] interceptors = DefaultProxyClassFactory.getProxyMethod(method).interceptors;
    return createActionInfo(mi, o, method, interceptors, paraResolvers, call, exceptionHandlerBeans);
  }


  protected void doConfigCORS(Action action, CORS CORSConfig) {

    String allowOrigin = CORSConfig.allowOrigin();
    String allowCredentials = CORSConfig.allowCredentials();
    String allowHeaders = CORSConfig.allowHeaders();
    String allowMethods = CORSConfig.allowMethods();
    String exposeHeaders = CORSConfig.exposeHeaders();
    String requestHeaders = CORSConfig.requestHeaders();
    String requestMethod = CORSConfig.requestMethod();
    String origin = CORSConfig.origin();
    String maxAge = CORSConfig.maxAge();

    action.header("Access-Control-Allow-Origin", allowOrigin);
    action.header("Access-Control-Allow-Methods", allowMethods);
    action.header("Access-Control-Allow-Headers", allowHeaders);
    action.header("Access-Control-Max-Age", maxAge);
    action.header("Access-Control-Allow-Credentials", allowCredentials);

    if (StrKit.notBlank(exposeHeaders)) {
      action.header("Access-Control-Expose-Headers", exposeHeaders);
    }

    if (StrKit.notBlank(requestHeaders)) {
      action.header("Access-Control-Request-Headers", requestHeaders);
    }

    if (StrKit.notBlank(requestMethod)) {
      action.header("Access-Control-Request-Method", requestMethod);
    }

    if (StrKit.notBlank(origin)) {
      action.header("Origin", origin);
    }
  }

}
