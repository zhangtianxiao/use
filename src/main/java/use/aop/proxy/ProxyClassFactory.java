
package use.aop.proxy;

@FunctionalInterface
public interface ProxyClassFactory {
  Class<?> get(Class<?> t);
}
