package use.beans;

@FunctionalInterface
public interface FieldGetter<T> {
  Object get(T o);
}
