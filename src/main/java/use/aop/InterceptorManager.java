

package use.aop;

import use.kit.BeanFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Function;

import static use.kit.ReflectKit.ifPresent;

/**
 InterceptorManager.
 1：管理控制层、业务层全局拦截器
 2：缓存业务层 Class 级拦截器数组。业务层拦截器被整体缓存在 ProxyMethod 中
 3：用于创建 Interceptor、组装 Interceptor
 4：除手动 new 出来的拦截器以外，其它所有拦截器均为单例

 无法使用 Method 或 Before 对象缓存业务层 Method 级拦截器：
 1：不同对象或相同对象获取同一个 Class 中同一个 Method 得到的对象 id 值不相同
 2：不同对象获取同一个 method 之上的 Before 得到的对象 id 值不相同
 */
public class InterceptorManager {

  public static final Interceptor[] NULL_INTERS = new Interceptor[0];

  public static final InterceptorManager me = new InterceptorManager();

  private InterceptorManager() {
  }

  public Interceptor[] buildInterceptors(Class<?> targetClass, Method method, BeanFactory beanFactory) {
    LinkedList<Class<? extends Interceptor>> classes = new LinkedList<>();
    //
    ifPresent(targetClass, Before.class, anno -> {
      classes.addAll(Arrays.asList(anno.value()));
      return null;
    });

    // 方法上的Clear, 用于清除类上的拦截器
    ifPresent(method, Clear.class, anno -> {
      classes.removeAll(Arrays.asList(anno.value()));
      return null;
    });

    ifPresent(method, Before.class, anno -> {
      classes.addAll(Arrays.asList(anno.value()));
      return null;
    });
    if (classes.size() == 0)
      return NULL_INTERS;

    // 创建
    Interceptor[] ret = new Interceptor[classes.size()];
    Iterator<Class<? extends Interceptor>> iterator = classes.iterator();
    int i = -1;
    while (iterator.hasNext()) {
      i++;
      Class<? extends Interceptor> next = iterator.next();
      ret[i] = beanFactory.get(next);
    }
    return ret;
  }
}



