
package use.template.expr.ast;

import use.template.Env;
import use.template.TemplateException;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

import java.lang.reflect.Field;

/**
 StaticField : ID_list '::' ID
 动态获取静态变量值，变量值改变时仍可正确获取
 用法：use.core.Const::JFINAL_VERSION
 */
public class StaticField extends Expr {

  private final Class<?> clazz;
  private final String fieldName;
  private final Field field;

  public StaticField(String className, String fieldName, Location location) {
    try {
      this.clazz = Class.forName(className);
      this.fieldName = fieldName;
      this.field = clazz.getField(fieldName);
      this.location = location;
    } catch (Exception e) {
      throw new ParseException(e.getMessage(), location, e);
    }
  }

  @Override
  public Object eval(Scope scope, Env env) {
    try {
      return field.get(null);
    } catch (Exception e) {
      throw new TemplateException(e.getMessage(), location, e);
    }
  }

  public String toString() {
    return clazz.getName() + "::" + fieldName;
  }
}







