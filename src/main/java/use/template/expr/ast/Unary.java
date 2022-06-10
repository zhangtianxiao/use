
package use.template.expr.ast;

import use.template.Env;
import use.template.TemplateException;
import use.template.expr.Sym;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 unary : ('!' | '+' | '-'| '++' | '--') expr

 只支持 +expr 与 -expr
 !expr、 ++expr、 --expr 分别由 Logic、IncDec 支持
 */
public class Unary extends Expr {

  private final Sym op;
  private final Expr expr;

  public Unary(Sym op, Expr expr, Location location) {
    if (expr == null) {
      throw new ParseException("The parameter of \"" + op.value() + "\" operator can not be blank", location);
    }
    this.op = op;
    this.expr = expr;
    this.location = location;
  }

  /**
   unary : ('!' | '+' | '-'| '++' | '--') expr
   */
  @Override
  public Object eval(Scope scope, Env env) {
    Object value = expr.eval(scope, env);
    if (value == null) {
      if (scope.getCtrl().isNullSafe()) {
        return null;
      }
      throw new TemplateException("The parameter of \"" + op.value() + "\" operator can not be blank", location);
    }
    if (!(value instanceof Number)) {
      throw new TemplateException(op.value() + " operator only support int long float double short byte BigDecimal BigInteger type", location);
    }

    switch (op) {
      case ADD:
        return value;
      case SUB:
        Number n = (Number) value;
        if (n instanceof Integer) {
          return -n.intValue();
        }
        if (n instanceof Long) {
          return -n.longValue();
        }
        if (n instanceof Float) {
          return -n.floatValue();
        }
        if (n instanceof Double) {
          return -n.doubleValue();
        }
        if (n instanceof BigDecimal) {
          return ((BigDecimal) n).negate();
        }
        if (n instanceof BigInteger) {
          return ((BigInteger) n).negate();
        }
        if (n instanceof Short || n instanceof Byte) {
          // short、byte 取负时转换为 int 型， java 语言亦如此
          return -n.intValue();
        }

        throw new TemplateException("Unsupported data type: " + n.getClass().getName(), location);
      default:
        throw new TemplateException("Unsupported operator: " + op.value(), location);
    }
  }

  /**
   如果可能的话，将 Unary 表达式转化成 Const 表达式，类似于 ExprParser.buildMapEntry() 需要这种转化来简化实现
   除了可简化程序外，还起到一定的性能优化作用

   Number : +123 -456 +3.14 -0.12
   Boolean : !true !false

   特别注意：
   Boolean 的支持并不需要，!true、!false 已在 ExprParser 中被 Logic 表达式接管，在此仅为逻辑上的完备性而添加
   */
  public Expr toConstIfPossible() {
    if (expr instanceof Const && (op == Sym.SUB || op == Sym.ADD || op == Sym.NOT)) {
    } else {
      return this;
    }

    Expr ret = this;
    Const c = (Const) expr;
    if (op == Sym.SUB) {
      if (c.isInt()) {
        ret = new Const(Sym.INT, -c.getInt());
      } else if (c.isLong()) {
        ret = new Const(Sym.LONG, -c.getLong());
      } else if (c.isFloat()) {
        ret = new Const(Sym.FLOAT, -c.getFloat());
      } else if (c.isDouble()) {
        ret = new Const(Sym.DOUBLE, -c.getDouble());
      }
    } else if (op == Sym.ADD) {
      if (c.isNumber()) {
        ret = c;
      }
    } else if (op == Sym.NOT) {
      if (c.isBoolean()) {
        ret = c.isTrue() ? Const.FALSE : Const.TRUE;
      }
    }

    return ret;
  }

  public String toString() {
    return op.toString() + expr.toString();
  }
}





