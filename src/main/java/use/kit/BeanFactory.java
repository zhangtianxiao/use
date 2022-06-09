package use.kit;

@FunctionalInterface
public interface BeanFactory {
  <T> T get(Class<T> t);
}
