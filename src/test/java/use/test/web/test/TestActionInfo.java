package use.test.web.test;

import use.aop.Interceptor;
import use.aop.Callback;
import use.mvc.router.ActionInfo;
import use.mvc.mi.ExceptionHandler;
import use.mvc.mi.MappingInfo;
import use.mvc.pararesolver.ParaResolver;

import java.lang.reflect.Method;

public class TestActionInfo extends ActionInfo {

  int total_access = 0;
  int valid_access = 0;

  public TestActionInfo(MappingInfo mi, Object o, Method method, Interceptor[] interceptors, ParaResolver[] paraResolvers, Callback dispelCall, ExceptionHandler[] exceptionHandlers) {
    super(mi, o, method, interceptors, paraResolvers, dispelCall, exceptionHandlers);
  }
}
