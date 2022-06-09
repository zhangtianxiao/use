
package use.template.expr.ast;

import use.template.TemplateException;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

import java.util.List;

/**
 index : 赋值表达式

 支持 a[i]、 a[b[i]]、a[i][j]、a[i][j]...[n] */
public class Index extends Expr {

  private final Expr expr;
  private final Expr index;

  public Index(Expr expr, Expr index, Location location) {
    if (expr == null || index == null) {
      throw new ParseException("array/list/map and their index can not be null", location);
    }
    this.expr = expr;
    this.index = index;
    this.location = location;
  }

  @SuppressWarnings("rawtypes")
  public Object eval(Scope scope) {
    Object target = expr.eval(scope);
    if (target == null) {
      if (scope.getCtrl().isNullSafe()) {
        return null;
      }
      throw new TemplateException("The index access operation target can not be null", location);
    }

    Object idx = index.eval(scope);
    if (idx == null) {
      if (scope.getCtrl().isNullSafe()) {
        return null;
      }

      if (target instanceof java.util.Map) {
        // Map 的 key 可以是 null，不能抛异常
      }
      else {
        throw new TemplateException("The index of list and array can not be null", location);
      }
    }

    if (target instanceof List) {
      if (idx instanceof Integer) {
        return ((List<?>) target).get((Integer) idx);
      }
      throw new TemplateException("The index of list must be integer", location);
    }

    if (target instanceof java.util.Map) {
      return ((java.util.Map) target).get(idx);
    }

    if (target.getClass().isArray()) {
      if (idx instanceof Number) {
        return java.lang.reflect.Array.get(target, ((Number) idx).intValue());
      }
      throw new TemplateException("The index of array must be Number", location);
    }
    String fieldName = (String) idx;
    try {
      Object resolve = scope.beans.get(target, fieldName);
      if (resolve == null)
        if (scope.getCtrl().isNullSafe()) {
          return null;
        }
        else
          throw new TemplateException("Only the list array and map is supported by index access", location);
      return resolve;
    } catch (TemplateException | ParseException e) {
      throw e;
    } catch (Exception e) {
      throw new TemplateException(e.getMessage(), location, e);
    }


  }
}




