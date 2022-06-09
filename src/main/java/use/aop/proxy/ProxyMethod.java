
package use.aop.proxy;

import use.aop.Interceptor;
import use.aop.InterceptorManager;
import use.kit.BeanFactory;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 ProxyMethod

 在 ProxyFactory 生成、编译、加载代理类彻底完成之后，
 再将 ProxyMethod 放入缓存，避免中途出现异常时缓存
 不完整的 ProxyMethod 对象
 */
public class ProxyMethod {

  public final Integer key;
  public final Method method;
  public final Interceptor[] interceptors;

  public ProxyMethod(Integer key, Method method, Class<?> targetClass, BeanFactory beanFactory) {
    this.key = key;
    this.method = method;
    this.interceptors = InterceptorManager.me.buildInterceptors(targetClass, method, beanFactory);
  }
}


