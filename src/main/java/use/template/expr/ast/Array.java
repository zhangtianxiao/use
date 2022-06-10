
package use.template.expr.ast;

import use.template.Env;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 Array

 用法：
 1：[1, 2, 3]
 2：["a", 1, "b", 2, false, 3.14]
 */
public class Array extends Expr {

  private final Expr[] exprList;

  public Array(Expr[] exprList, Location location) {
    if (exprList == null) {
      throw new ParseException("exprList can not be null", location);
    }
    this.exprList = exprList;
  }

  @Override
  public Object eval(Scope scope, Env env) {
    List<Object> array = new ArrayListExt(exprList.length);
    for (Expr expr : exprList) {
      array.add(expr.eval(scope, env));
    }
    return array;
  }

  /**
   支持 array.length 表达式
   */
  @SuppressWarnings("serial")
  public static class ArrayListExt extends ArrayList<Object> {

    public ArrayListExt(int initialCapacity) {
      super(initialCapacity);
    }

    public Integer getLength() {
      return size();
    }
  }
}



