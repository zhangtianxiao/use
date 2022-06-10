
package use.template.expr.ast;

import use.template.Env;
import use.template.stat.Ctrl;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

/**
 NullSafe
 在原则上只支持具有动态特征的用法，例如：方法调用、字段取值、Map 与 List 取值
 而不支持具有静态特征的用法，例如：static method 调用、shared method 调用

 用法：
 #( seoTitle ?? "JFinal 极速开发社区" )
 支持级联：  #( a.b.c ?? "JFinal 极速开发社区" )
 支持嵌套：  #( a ?? b ?? c ?? d)
 */
public class NullSafe extends Expr {

  private final Expr left;
  private final Expr right;

  public NullSafe(Expr left, Expr right, Location location) {
    if (left == null) {
      throw new ParseException("The expression on the left side of null coalescing and safe access operator \"??\" can not be blank", location);
    }
    this.left = left;
    this.right = right;
    this.location = location;
  }

  @Override
  public Object eval(Scope scope, Env env) {
    Ctrl ctrl = scope.getCtrl();
    boolean oldNullSafeValue = ctrl.isNullSafe();

    try {
      ctrl.setNullSafe(true);
      Object ret = left.eval(scope, env);
      if (ret != null) {
        return ret;
      }
    } finally {
      ctrl.setNullSafe(oldNullSafeValue);
    }

    // right 表达式处于 null safe 区域之外
    return right != null ? right.eval(scope, env) : null;
  }
}







