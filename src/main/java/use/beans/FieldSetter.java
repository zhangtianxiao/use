package use.beans;

@FunctionalInterface
public interface FieldSetter<T> {
  Object set(T o, Object value);
}
