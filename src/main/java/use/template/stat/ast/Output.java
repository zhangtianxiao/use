

package use.template.stat.ast;

import use.template.io.Writer;
import use.template.Env;
import use.template.TemplateException;
import use.template.expr.ast.Expr;
import use.template.expr.ast.ExprList;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

/**
 Output 输出指令

 用法：
 1：#(value)
 2：#(x = 1, y = 2, x + y)
 3：#(seoTitle ?? 'JFinal 极速开发社区')
 */
public class Output extends Stat {

  protected Expr expr;

  public Output(ExprList exprList, Location location) {
    if (exprList.length() == 0) {
      throw new ParseException("The expression of output directive like #(expression) can not be blank", location);
    }
    this.expr = exprList.getActualExpr();
  }

  public void exec(Env env, Scope scope, Writer writer) {
    try {
      Object value = expr.eval(scope, env);
      writer.writeVar(value);
      /*if (value instanceof String) {
        String str = (String) value;
        writer.writeVal(str);
      } else if (value instanceof Number) {
        Class<?> c = value.getClass();
        if (c == Integer.class) {
          writer.writeVal((Integer) value);
        } else if (c == Long.class) {
          writer.writeVal((Long) value);
        } else if (c == Double.class) {
          writer.writeVal((Double) value);
        } else if (c == Float.class) {
          writer.writeVal((Float) value);
        } else {
          writer.writeVal(value.toString());
        }
      } else if (value != null) {
        writer.writeVal(value.toString());
      }else{
        writer.writeNull();
      }*/
    } catch (TemplateException | ParseException e) {
      throw e;
    } catch (Exception e) {
      throw new TemplateException(e.getMessage(), location, e);
    }
  }

  public Object eval(Scope scope, Env env) {
    return expr.eval(scope, env);
  }

  public Object eval(Scope scope) {
    return expr.eval(scope, null);
  }

  public Object eval() {
    return expr.eval(null,null);
  }
}




