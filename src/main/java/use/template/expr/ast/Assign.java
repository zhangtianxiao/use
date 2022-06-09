

package use.template.expr.ast;

import use.beans.FieldDesc;
import use.beans.FieldKeyBuilder;
import use.template.TemplateException;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

import java.util.List;
import java.util.Map;

/**
 Assign

 支持三种赋值，其中第二种如果括号中是 ID 或 STR 则演变为第三种是对 map 赋值：
 1：ID = expr
 2：ID [ expr ] = expr
 如果 expr 为 int 或 long 型，则是对 array 赋值
 如果 expr 为 ID、STR 型，则是对 map 进行赋值
 否则抛异常出来
 3：ID [ ID ] = expr 或者 ID [ STR ] = expr
 4：支持无限连：id = array[ i = 0 ] = array[1] = 123
 */
public class Assign extends Expr {

  private final String id;
  private final Expr index;  // index 用于支持 ID [ expr ] = expr 这种形式
  private final Expr right;

  /**
   数组赋值表达式
   */
  public Assign(String id, Expr index, Expr right, Location location) {
    if (index == null) {
      throw new ParseException("The index expression of array assignment can not be null", location);
    }
    if (right == null) {
      throw new ParseException("The expression on the right side of an assignment expression can not be null", location);
    }
    this.id = id;
    this.index = index;
    this.right = right;
    this.location = location;
  }

  /**
   普通赋值表达式
   */
  public Assign(String id, Expr right, Location location) {
    if (right == null) {
      throw new ParseException("The expression on the right side of an assignment expression can not be null", location);
    }
    this.id = id;
    this.index = null;
    this.right = right;
    this.location = location;
  }

  /**
   获取 assign 表达式左侧标识符 id
   在自定义指令中得到 id 值，可以得知该赋值表达式是针对哪个变量在操作，有助于扩展
   需求来源：https://jfinal.com/share/379
   */
  public String getId() {
    return id;
  }

  public Expr getIndex() {
    return index;
  }

  public Expr getRight() {
    return right;
  }

  /**
   赋值语句有返回值，可以用于表达式计算
   */
  public Object eval(Scope scope) {
    if (index == null) {
      return assignVariable(scope);
    } else {
      return assignElement(scope);
    }
  }

  Object assignVariable(Scope scope) {
    Object rightValue = right.eval(scope);
    if (scope.getCtrl().isWisdomAssignment()) {
      scope.set(id, rightValue);
    } else if (scope.getCtrl().isLocalAssignment()) {
      scope.setLocal(id, rightValue);
    } else {
      scope.setGlobal(id, rightValue);
    }

    return rightValue;
  }

  /**
   数组或 Map 赋值
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  Object assignElement(Scope scope) {
    Object target = scope.get(id);
    if (target == null) {
      throw new TemplateException("assigned targets \"" + id + "\" can not be null", location);
    }
    Object idx = index.eval(scope);
    if (idx == null) {
      throw new TemplateException("assigned key of map can not be null", location);
    }

    Object value;
    if (target instanceof Map) {
      value = right.eval(scope);
      ((Map) target).put(idx, value);
      return value;
    }

    // 扩展
    // 反射赋值
    if (!(idx instanceof Integer)) {
      //throw new TemplateException("The index of list/array can only be integer", location);
      //scope.fg.getFieldGetter()
      String fieldName = (String) idx;
      try {
        value = right.eval(scope);
        long fieldNameHash = index instanceof Const ? ((Const) index).fnv1a64 : FieldKeyBuilder.me.fieldHash(fieldName);
        //long fieldNameHash = HashKit.fnv1a64(fieldName);
        FieldDesc fieldDesc = scope.beans.desc(target, fieldNameHash);
        if (fieldDesc != null)
          return fieldDesc.setter.set(target, value);
        else
          throw new TemplateException("未注册的赋值行为: " + target + "  " + fieldName, location);
      } catch (TemplateException | ParseException e) {
        throw e;
      } catch (Exception e) {
        throw new TemplateException(target + "  " + fieldName, location, e);
      }
    }

    if (target instanceof List) {
      value = right.eval(scope);
      ((List) target).set((Integer) idx, value);
      return value;
    }
    if (target.getClass().isArray()) {
      value = right.eval(scope);
      java.lang.reflect.Array.set(target, (Integer) idx, value);
      return value;
    }

    throw new TemplateException("Only the list array and map is supported by index assignment", location);
  }
}







