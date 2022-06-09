package use.jdbc.graph;

import use.beans.FieldDesc;
import use.jdbc.RsGetter;

import java.lang.reflect.Field;
import java.util.Objects;

public abstract class GraphFieldInfo {
  final boolean isList;
  final boolean isPrimitive;
  final boolean isAny;

  final Field field;
  final RsGetter rsGetter;
  final FieldDesc fieldDesc;

  public GraphFieldInfo(boolean isList, boolean isPrimitive, boolean isAny, Field field, RsGetter rsGetter, FieldDesc fieldDesc) {
    field.setAccessible(true);
    this.isList = isList;
    this.isPrimitive = isPrimitive;
    this.isAny = isAny;
    this.field = field;
    this.rsGetter = rsGetter;
    this.fieldDesc = fieldDesc;
  }


  boolean isNs() {
    return false;
  }

  ClassInfo classInfo;

  boolean isBean() {
    return !(isPrimitive || isAny);
  }
}


class PrimitiveFieldInfo extends GraphFieldInfo {
  public PrimitiveFieldInfo(boolean isList, boolean isPrimitive, boolean isAny, Field field, RsGetter rsGetter, FieldDesc fieldDesc) {
    super(isList, isPrimitive, isAny, field, Objects.requireNonNull(rsGetter), fieldDesc);
  }
}


