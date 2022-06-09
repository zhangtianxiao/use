package use.beans;

public class FieldDesc<T> {
  public static <T> FieldDesc<T> create(Class<T> c, String name, FieldSetter<T> setter, FieldGetter<T> getter) {
    return new FieldDesc<T>(c, name, setter, getter);
  }

  public static <T> FieldDesc<T> create(Class<T> c, String name, FieldGetter<T> getter, FieldSetter<T> setter) {
    return new FieldDesc<T>(c, name, setter, getter);
  }

  public final Class<T> c;
  public final String name;
  public final FieldSetter<T> setter;
  public final FieldGetter<T> getter;

  public FieldDesc(Class<T> c, String name, FieldSetter<T> setter, FieldGetter<T> getter) {
    this.c = c;
    this.name = name;
    this.setter = setter;
    this.getter = getter;
  }
}