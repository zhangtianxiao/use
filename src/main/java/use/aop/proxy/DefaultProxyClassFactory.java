package use.aop.proxy;

import cn.hutool.core.util.ReflectUtil;
import org.slf4j.Logger;
import use.aop.Before;
import use.kit.BeanFactory;
import use.kit.Helper;
import use.kit.ReflectKit;
import use.kit.SyncWriteMap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultProxyClassFactory implements ProxyClassFactory {
  final BeanFactory beanFactory;

  public DefaultProxyClassFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  private static final Logger logger = Helper.getLogger(DefaultProxyClassFactory.class);
  protected static ConcurrentHashMap<Class<?>, Class<?>> proxyClassCache = new ConcurrentHashMap<>();

  private static final Map<Integer, ProxyMethod> PROXY_METHOD_CACHE = new SyncWriteMap<>();

  public static ProxyMethod getProxyMethod(Method method) {
    return PROXY_METHOD_CACHE.get(ReflectKit.hash(method));
  }

  public static ProxyMethod getProxyMethod(int hash) {
    return PROXY_METHOD_CACHE.get(hash);
  }

  static synchronized void put(ProxyMethod proxyMethod) {
    if (PROXY_METHOD_CACHE.containsKey(proxyMethod)) {
      throw new RuntimeException("the key of proxyMethod already exists");
    }
    PROXY_METHOD_CACHE.put(proxyMethod.key, proxyMethod);
  }

  static <T> boolean canProxy(Class<T> target) {
    // 在此不对 static 类做检测，支持对 static 类的代理
    int mod = target.getModifiers();
    if (!Modifier.isPublic(mod)) {
      return false;
    }
    if (Modifier.isFinal(mod)) {
      return false;
    }
    if (Modifier.isAbstract(mod)) {
      return false;
    }
    boolean b = target.isAnnotationPresent(Before.class);
    if (!b) b = Arrays.stream(ReflectUtil.getMethods(target)).anyMatch(it -> it.isAnnotationPresent(Before.class));
    return b;
  }

  @Override
  public Class<?> get(Class<?> target) {
    if (target.getSimpleName().contains("$$")) return target;
    if (!canProxy(target)) return target;

    /**
     *  用target class作为锁, 应能更容易发现递归注入
     * */
    synchronized (target) {
      Class<?> ret = proxyClassCache.get(target);
      if (ret != null) {
        return ret;
      }

      if (ProxyGenerator.me.needProxy(target)) {
        ProxyClass proxyClass = ProxyGenerator.me.generate(target, beanFactory);
        logger.info("将生成代理类: " + target);
        ProxyCompiler.me.compile(proxyClass);
        ret = ProxyClassLoader.me.loadProxyClass(proxyClass);
        for (ProxyMethod m : proxyClass.getProxyMethodList()) {
          put(m);
        }
        proxyClassCache.put(target, ret);
        return ret;
      } else {
        proxyClassCache.put(target, target);    // 无需代理的情况映射原参数 target
        return target;
      }
    }
  }
}
