package use.kit;

public interface BeanHolder<T> {
  T get();

  void destroy();
}
