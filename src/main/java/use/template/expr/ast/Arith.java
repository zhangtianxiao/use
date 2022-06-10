
package use.template.expr.ast;

import use.template.Env;
import use.template.TemplateException;
import use.template.expr.Sym;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 Arithmetic
 1：支持 byte short int long float double BigInteger BigDecimal 的 + - * / % 运算
 2：支持字符串加法运算
 */
public class Arith extends Expr {

  public static final int INT = 0;  // byte、short 用 int 类型支持，java 表达式亦如此
  public static final int LONG = 1;
  public static final int FLOAT = 2;
  public static final int DOUBLE = 3;
  public static final int BIGINTEGER = 4;
  public static final int BIGDECIMAL = 5;
  public static final int UNKNOWN = 99;

  private final Sym op;
  private final Expr left;
  private final Expr right;
  private final boolean isConst;
  private final Object value;


  public Arith(Sym op, Expr left, Expr right, Location location) {
    if (left == null || right == null) {
      throw new ParseException("The target of \"" + op.value() + "\" operator can not be blank", location);
    }
    this.op = op;
    this.left = left;
    this.right = right;
    //  常量
    if (left instanceof Const && right instanceof Const)
      isConst = true;
    else
      // 两侧都是常量
      isConst = left instanceof Arith && ((Arith) left).isConst && right instanceof Const;
    if (isConst)
      value = doEval(null, null);
    else
      value = null;
    this.location = location;
  /*  String s = this.toString();
    if(!s.contains("@")){
      //
    }else{
      System.out.println(s);
      System.out.println("不可启用动态编译");
    }*/
  }


  // BigDecimal 除法使用的最小 scale 值，默认为 5
  protected static int bigDecimalDivideMinScale = 5;
  // BigDecimal 除法使用的舍入模式，默认为四舍五入
  public static RoundingMode bigDecimalDivideRoundingMode = RoundingMode.HALF_UP;

  /**
   设置 BigDecimal 除法使用的最小 scale 值，默认为 5
   */
  public static void setBigDecimalDivideMinScale(int scale) {
    Arith.bigDecimalDivideMinScale = scale;
  }

  /**
   设置 BigDecimal 除法使用的舍入模式，默认为四舍五入
   */
  public static void setBigDecimalDivideRoundingMode(RoundingMode roundingMode) {
    Objects.requireNonNull(roundingMode, "roundingMode can not be null");
    Arith.bigDecimalDivideRoundingMode = roundingMode;
  }


  @Override
  public Object eval(Scope scope, Env env) {
    if (isConst)
      return value;
    try {
      return doEval(scope, env);
    } catch (TemplateException e) {
      throw e;
    } catch (Exception e) {
      throw new TemplateException(e.getMessage(), location, e);
    }
  }


  private Object doEval(Scope scope, Env env) {
    Object leftValue = left.eval(scope, env);
    Object rightValue = right.eval(scope, env);
/*
   if (true) {
      final BigDecimal l = (BigDecimal) leftValue;
      final BigDecimal r = (BigDecimal) rightValue;
      switch (op) {
        case ADD:
          return l.add(r);
        case SUB:
          return l.subtract(r);
        case MUL:
          return l.multiply(r);
        case DIV:
          return l.divide(r, bigDecimalDivideRoundingMode);
        case MOD:
          return l.remainder(r);
        default:
          throw new TemplateException("Unsupported operator: " + op.value(), location);
      }

    }
*/
    if (leftValue instanceof Number && rightValue instanceof Number) {
      Number l = (Number) leftValue;
      Number r = (Number) rightValue;
      int maxType = getMaxType(l, r);
      if (maxType == UNKNOWN) {
        throw unsupportedTypeException(l, r, location);
      }

      switch (op) {
        case ADD:
          return add(maxType, l, r);
        case SUB:
          return sub(maxType, l, r);
        case MUL:
          return mul(maxType, l, r);
        case DIV:
          return div(maxType, l, r);
        case MOD:
          return remainder(maxType, l, r);
        default:
          throw new TemplateException("Unsupported operator: " + op.value(), location);
      }
    }

    // 字符串加法运算
    if (op == Sym.ADD) {
      if (leftValue instanceof String || rightValue instanceof String) {
        return String.valueOf(leftValue).concat(String.valueOf(rightValue));
      }
    }

    String leftObj = leftValue != null ? leftValue.getClass().getName() : "null";
    String rightObj = rightValue != null ? rightValue.getClass().getName() : "null";
    throw new TemplateException("Unsupported operation type: " + leftObj + " " + op.value() + " " + rightObj, location);
  }

  static BigDecimal[] toBigDecimals(Number left, Number right) {
    BigDecimal[] ret = new BigDecimal[2];

    if (left instanceof BigDecimal) {
      ret[0] = (BigDecimal) left;
    } else {
      ret[0] = new BigDecimal(left.toString());
    }

    if (right instanceof BigDecimal) {
      ret[1] = (BigDecimal) right;
    } else {
      ret[1] = new BigDecimal(right.toString());
    }

    return ret;
  }

  static void toBigDecimals(BigDecimal[] ret, Number left, Number right) {

    if (left instanceof BigDecimal) {
      ret[0] = (BigDecimal) left;
    } else {
      ret[0] = new BigDecimal(left.toString());
    }

    if (right instanceof BigDecimal) {
      ret[1] = (BigDecimal) right;
    } else {
      ret[1] = new BigDecimal(right.toString());
    }
  }

  static BigInteger[] toBigIntegers(Number left, Number right) {
    BigInteger[] ret = new BigInteger[2];

    if (left instanceof BigInteger) {
      ret[0] = (BigInteger) left;
    } else {
      ret[0] = new BigInteger(left.toString());
    }

    if (right instanceof BigInteger) {
      ret[1] = (BigInteger) right;
    } else {
      ret[1] = new BigInteger(right.toString());
    }

    return ret;
  }

  static void toBigIntegers(BigInteger[] ret, Number left, Number right) {

    if (left instanceof BigInteger) {
      ret[0] = (BigInteger) left;
    } else {
      ret[0] = new BigInteger(left.toString());
    }

    if (right instanceof BigInteger) {
      ret[1] = (BigInteger) right;
    } else {
      ret[1] = new BigInteger(right.toString());
    }
  }

  static int getMaxType(Number obj1, Number obj2) {
    int t1 = getType(obj1);
    int t2 = getType(obj2);
    int ret = Math.max(t1, t2);
    if (ret != BIGINTEGER) {
      return ret;
    }

    // BigInteger 在与 Double、Float 运算时，需要升级为 BigDecimal
    if (t1 == BIGINTEGER) {
      if (t2 == DOUBLE || t2 == FLOAT) {
        return BIGDECIMAL;  // 升级为 BigDecimal
      }
    } else {
      if (t1 == DOUBLE || t1 == FLOAT) {
        return BIGDECIMAL;  // 升级为 BigDecimal
      }
    }

    return ret;
  }

  static int getType(Number obj) {
    if (obj instanceof Integer) {
      //if (Integer.class.isAssignableFrom(obj.getClass())) {
      return INT;
    } else if (obj instanceof Long) {
      return LONG;
    } else if (obj instanceof Float) {
      return FLOAT;
    } else if (obj instanceof Double) {
      return DOUBLE;
    } else if (obj instanceof BigDecimal) {
      return BIGDECIMAL;
    } else if (obj instanceof Short || obj instanceof Byte) {
      return INT;      // short byte 用 int 支持，java 表达式亦如此
    } else if (obj instanceof BigInteger) {
      return BIGINTEGER;  // 新增 BigInteger 支持
    }

    // throw new TemplateException("Unsupported data type: " + obj.getClass().getName(), location);
    return UNKNOWN;
  }

  private Number add(int maxType, Number left, Number right) {
    switch (maxType) {
      case INT:
        //return Integer.valueOf(left.intValue() + right.intValue());
        return (Integer) left + (Integer) right;
      case LONG:
        return Long.valueOf(left.longValue() + right.longValue());
      case FLOAT:
        return Float.valueOf(left.floatValue() + right.floatValue());
      case DOUBLE:
        return Double.valueOf(left.doubleValue() + right.doubleValue());
      case BIGDECIMAL:
        BigDecimal[] bd = toBigDecimals(left, right);
        return (bd[0]).add(bd[1]);
      case BIGINTEGER:  // 新增 BigInteger 支持
        BigInteger[] bi = toBigIntegers(left, right);
        return (bi[0]).add(bi[1]);
    }
    throw new TemplateException("Unsupported data type", location);
  }

  private Number sub(int maxType, Number left, Number right) {
    switch (maxType) {
      case INT:
        return Integer.valueOf(left.intValue() - right.intValue());
      case LONG:
        return Long.valueOf(left.longValue() - right.longValue());
      case FLOAT:
        return Float.valueOf(left.floatValue() - right.floatValue());
      case DOUBLE:
        return Double.valueOf(left.doubleValue() - right.doubleValue());
      case BIGDECIMAL:
        BigDecimal[] bd = toBigDecimals(left, right);
        return (bd[0]).subtract(bd[1]);
      case BIGINTEGER:  // 新增 BigInteger 支持
        BigInteger[] bi = toBigIntegers(left, right);
        return (bi[0]).subtract(bi[1]);
    }
    throw new TemplateException("Unsupported data type", location);
  }

  private Number mul(int maxType, Number left, Number right) {
    switch (maxType) {
      case INT:
        return Integer.valueOf(left.intValue() * right.intValue());
      case LONG:
        return Long.valueOf(left.longValue() * right.longValue());
      case FLOAT:
        return Float.valueOf(left.floatValue() * right.floatValue());
      case DOUBLE:
        return Double.valueOf(left.doubleValue() * right.doubleValue());
      case BIGDECIMAL:
        BigDecimal[] bd = toBigDecimals(left, right);
        return (bd[0]).multiply(bd[1]);
      case BIGINTEGER:  // 新增 BigInteger 支持
        BigInteger[] bi = toBigIntegers(left, right);
        return (bi[0]).multiply(bi[1]);
    }
    throw new TemplateException("Unsupported data type", location);
  }

  private Number div(int maxType, Number left, Number right) {
    switch (maxType) {
      case INT:
        return Integer.valueOf(left.intValue() / right.intValue());
      case LONG:
        return Long.valueOf(left.longValue() / right.longValue());
      case FLOAT:
        return Float.valueOf(left.floatValue() / right.floatValue());
      case DOUBLE:
        return Double.valueOf(left.doubleValue() / right.doubleValue());
      case BIGDECIMAL:
        BigDecimal[] bd = toBigDecimals(left, right);
        // return (bd[0]).divide(bd[1]);
        int scale = Math.max(bigDecimalDivideMinScale, bd[0].scale());
        return (bd[0]).divide(bd[1], scale, bigDecimalDivideRoundingMode);
      case BIGINTEGER:  // 新增 BigInteger 支持
        BigInteger[] bi = toBigIntegers(left, right);
        return (bi[0]).divide(bi[1]);
    }
    throw new TemplateException("Unsupported data type", location);
  }

  private Number remainder(int maxType, Number left, Number right) {
    switch (maxType) {
      case INT:
        return Integer.valueOf(left.intValue() % right.intValue());
      case LONG:
        return Long.valueOf(left.longValue() % right.longValue());
      case FLOAT:
        return Float.valueOf(left.floatValue() % right.floatValue());
      case DOUBLE:
        return Double.valueOf(left.doubleValue() % right.doubleValue());
      case BIGDECIMAL:
        BigDecimal[] bd = toBigDecimals(left, right);
        return (bd[0]).divideAndRemainder(bd[1])[1];
      case BIGINTEGER:  // 新增 BigInteger 支持
        BigInteger[] bi = toBigIntegers(left, right);
        return (bi[0]).divideAndRemainder(bi[1])[1];
    }
    throw new TemplateException("Unsupported data type", location);
  }

  static TemplateException unsupportedTypeException(Number left, Number right, Location location) {
    Number unsupportedType;
    if (left instanceof Integer
      || left instanceof Long
      || left instanceof Float
      || left instanceof Double
      || left instanceof BigDecimal
      || left instanceof Short
      || left instanceof Byte
      || left instanceof BigInteger) {
      unsupportedType = right;
    } else {
      unsupportedType = left;
    }
    return new TemplateException("Unsupported data type: " + unsupportedType.getClass().getName(), location);
  }

/*  @Override
  public String toString() {
    //return '(' + partToString(left) + op + partToString(right) + ')';
    try {
      return partToString(left) + '.' + opToFunction(op) + '(' + partToString(right) + (op==Sym.DIV?",bigDecimalDivideRoundingMode":"")+')';
    } catch (Exception e) {
      return super.toString();
    }
  }

  private static String opToFunction(Sym op) {
    if (op == Sym.ADD) {
      return "add";
    }
    if (op == Sym.DIV) {
      return "divide";
    }
    if (op == Sym.SUB) {
      return "subtract";
    }
    if (op == Sym.MUL) {
      return "multiply";
    }
    if (op == Sym.MOD) {
      return "remainder";
    }
    throw new Unsupported();

  }

  private static String partToString(Expr expr) {
    String part = "";
    if (expr instanceof Arith)
      part = expr.toString();
    else if (expr instanceof Id) {
      part = "scope.getBigDecimal(\"" + expr + "\")";
    } else if (expr instanceof Const) {
      part = "new BigDecimal(\"" + expr + "\")";
    } else {
      throw new Unsupported();
    }
    return part;
  }*/
}




