
package use.template.expr.ast;

import use.template.TemplateException;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

import java.util.AbstractList;

/**
 RangeArray : [expr .. expr]

 用法：
 1：[1..3]
 2：[3..1]
 */
public class RangeArray extends Expr {

  private final Expr start;
  private final Expr end;

  /**
   array : '[' exprList ? | range ? ']'
   exprList : expr (',' expr)*
   range : expr .. expr
   */
  public RangeArray(Expr start, Expr end, Location location) {
    if (start == null) {
      throw new ParseException("The start value of range array can not be blank", location);
    }
    if (end == null) {
      throw new ParseException("The end value of range array can not be blank", location);
    }
    this.start = start;
    this.end = end;
    this.location = location;
  }

  public Object eval(Scope scope) {
    Object startValue = start.eval(scope);
    if (!(startValue instanceof Integer)) {
      throw new TemplateException("The start value of range array must be Integer", location);
    }
    Object endValue = end.eval(scope);
    if (!(endValue instanceof Integer)) {
      throw new TemplateException("The end value of range array must be Integer", location);
    }

    return new RangeList((Integer) startValue, (Integer) endValue);
  }

  public static class RangeList extends AbstractList<Integer> {

    final int start;
    final int size;
    final boolean increase;

    public RangeList(int start, int end) {
      this.start = start;
      this.increase = (start <= end);
      this.size = Math.abs(end - start) + 1;
    }

    public Integer get(int index) {
      return increase ? start + index : start - index;
    }

    public int size() {
      return size;
    }
  }
}




