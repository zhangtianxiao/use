package use.jdbc.graph;

import use.beans.FieldDesc;
import use.jdbc.RsGetter;

import java.lang.reflect.Field;

class RefKeyInfo {
  int column;
  final Class<?> type;
  final String name;
  final FieldDesc fieldDesc;
  final RsGetter rsGetter;

  public RefKeyInfo(int column, Field field, RsGetter rsGetter, FieldDesc fieldDesc) {
    this.column = column;
    this.type = field.getType();
    this.name = field.getName();
    this.fieldDesc = fieldDesc;
    this.rsGetter = rsGetter;
  }
}
