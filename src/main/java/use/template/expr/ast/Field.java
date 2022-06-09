

package use.template.expr.ast;

import use.beans.FieldDesc;
import use.beans.FieldKeyBuilder;
import use.template.TemplateException;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

import java.util.Map;

/**
 Field 取值表达式

 field 表达式取值优先次序，以 user.name 为例
 1：假如 user.getName() 存在，则优先调用
 2：假如 user 具有 public name 属性，则取 user.name 属性值
 3：假如 user 为 Model 子类，则调用 user.get("name")
 4：假如 user 为 Record，则调用 user.get("name")
 5：假如 user 为 Map，则调用 user.get("name")
 */
public class Field extends Expr {

  private final Expr expr;
  public final String fieldName;
  public final long fieldNameHash;

  public Field(Expr expr, String fieldName, Location location) {
    if (expr == null) {
      throw new ParseException("The object for field access can not be null", location);
    }
    this.expr = expr;
    this.fieldName = fieldName;
    this.fieldNameHash = FieldKeyBuilder.me.fieldHash(fieldName);
    this.location = location;
  }

  public Object eval(Scope scope) {
    Object target = expr.eval(scope);
    if (target == null) {
      if (scope.getCtrl().isNullSafe()) {
        return null;
      }
      if (expr instanceof Id) {
        String id = ((Id) expr).getId();
        throw new TemplateException("\"" + id + "\" can not be null for accessed by \"" + id + "." + fieldName + "\"", location);
      }
      throw new TemplateException("Can not accessed by \"" + fieldName + "\" field from null target", location);
    }


    FieldDesc<Object> desc = scope.beans.desc(target, fieldNameHash);
    try {
      if (desc != null)
        return desc.getter.get(target);
        // 支持map
      else if (target instanceof Map<?, ?> map) {
        return map.get(fieldName);
      }
    } catch (TemplateException | ParseException e) {
      throw e;
    } catch (Exception e) {
      throw new TemplateException(e.getMessage(), location, e);
    }


    if (scope.getCtrl().isNullSafe()) {
      return null;
    }
    if (expr instanceof Id) {
      String id = ((Id) expr).getId();
      throw new TemplateException("public field not found: \"" + id + "." + fieldName + "\" and public getter method not found", location);
    }
    throw new TemplateException("public field not found: \"" + fieldName + "\" and public getter method not found", location);
  }

  // private Long buildFieldKey(Class<?> targetClass) {
  // return targetClass.getName().hashCode() ^ getterNameHash;
  // }
}






