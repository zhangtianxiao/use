
package use.aop;

/**
 Interceptor.
 处于性能考虑, 将Interceptor设计为单例, 保证实例不变, 进而可以在创建代理类时生成Lazy<Interceptor[]>
 */
public interface Interceptor {
  void intercept(Invocation inv);
}

