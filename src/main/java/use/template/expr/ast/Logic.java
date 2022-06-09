
package use.template.expr.ast;

import use.template.TemplateException;
import use.template.expr.Sym;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 Logic

 支持逻辑运算： !  &&  ||
 */
public class Logic extends Expr {

  private final Sym op;
  private final Expr left;    // ! 运算没有 left 参数
  private final Expr right;

  /**
   构造 || && 结点
   */
  public Logic(Sym op, Expr left, Expr right, Location location) {
    if (left == null) {
      throw new ParseException("The target of \"" + op.value() + "\" operator on the left side can not be blank", location);
    }
    if (right == null) {
      throw new ParseException("The target of \"" + op.value() + "\" operator on the right side can not be blank", location);
    }
    this.op = op;
    this.left = left;
    this.right = right;
    this.location = location;
  }

  /**
   构造 ! 结点，left 为 null
   */
  public Logic(Sym op, Expr right, Location location) {
    if (right == null) {
      throw new ParseException("The target of \"" + op.value() + "\" operator on the right side can not be blank", location);
    }
    this.op = op;
    this.left = null;
    this.right = right;
    this.location = location;
  }

  public Object eval(Scope scope) {
    switch (op) {
      case NOT:
        return evalNot(scope);
      case AND:
        return evalAnd(scope);
      case OR:
        return evalOr(scope);
      default:
        throw new TemplateException("Unsupported operator: " + op.value(), location);
    }
  }

  Object evalNot(Scope scope) {
    return !isTrue(right.eval(scope));
  }

  Object evalAnd(Scope scope) {
    return isTrue(left.eval(scope)) && isTrue(right.eval(scope));
  }

  Object evalOr(Scope scope) {
    return isTrue(left.eval(scope)) || isTrue(right.eval(scope));
  }

  /**
   规则：
   1：null 返回 false
   2：boolean 类型，原值返回
   3：String、StringBuilder 等一切继承自 CharSequence 类的对象，返回 length > 0
   4：其它返回 true
   */
  public static boolean isTrue(Object v) {
    if (v == null) {
      return false;
    }

    if (v instanceof Boolean) {
      return (Boolean) v;
    }

    if (v instanceof CharSequence) {
      return ((CharSequence) v).length() > 0;
    }

    // 扩展
    if (v instanceof Number) {
      return ((Double) v) > 0;
    }

    if (v instanceof Collection) {
      return ((Collection<?>) v).size() > 0;
    }

    if (v instanceof Map) {
      return ((Map<?, ?>) v).size() > 0;
    }

    if (v.getClass().isArray()) {
      return Array.getLength(v) > 0;
    }
    return true;
  }

  public static boolean isFalse(Object v) {
    return !isTrue(v);
  }
}



