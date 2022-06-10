
package use.template.ext.directive;

import use.template.Directive;
import use.template.Env;
import use.template.TemplateException;
import use.template.expr.ast.Expr;
import use.template.expr.ast.ExprList;
import use.template.io.Writer;
import use.template.stat.ParseException;
import use.template.stat.Scope;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 #number 数字格式化输出指令

 优化时要注意 DecimalFormat 并非线程安全

 两种用法：
 1：#number(n) 用默认 pattern 输出变量中的值
 2：#number(n, "#.##") 用第二个参数指定的 pattern 输出变量中的值

 注意：
 1：pattern 的使用与 java.text.DecimalFormat 的完全一样
 在拿不定主意的时候可以在搜索引擎中搜索关键字：DecimalFormat
 2：#number 指令中的参数可以是变量，例如：#number(n, p) 中的 n 与 p 可以全都是变量

 <pre>
 示例：
 #number(3.1415926, "#.##")
 #number(0.9518, "#.##%")
 #number(300000, "光速为每秒 ,### 公里。")
 #number(1299792458, ",###")	// 每三位以逗号进行分隔输出为：1,299,792,458

 #set(n = 1.234)
 #set(p = "#.##")
 #number(n, p)
 </pre>
 */
public class NumberDirective extends Directive {

  private Expr valueExpr;
  private Expr patternExpr;

  public void setExprList(ExprList exprList) {
    int paraNum = exprList.length();
    if (paraNum == 0) {
      throw new ParseException("The parameter of #number directive can not be blank", location);
    }
    if (paraNum > 2) {
      throw new ParseException("Wrong number parameter of #number directive, two parameters allowed at most", location);
    }

    valueExpr = exprList.getExpr(0);
    patternExpr = (paraNum == 1 ? null : exprList.getExpr(1));
  }

  public void exec(Env env, Scope scope, Writer writer) {
    Object value = valueExpr.eval(scope, env);
    if (value == null) {
      return;
    }

    RoundingMode roundingMode = env.config.getRoundingMode();
    if (patternExpr == null) {
      outputWithoutPattern(value, roundingMode, writer);
    } else {
      outputWithPattern(value, roundingMode, scope, env, writer);
    }
  }

  private void outputWithoutPattern(Object value, RoundingMode roundingMode, Writer writer) {
    DecimalFormat df = new DecimalFormat();
    df.setRoundingMode(roundingMode);

    String ret = df.format(value);
    write(writer, ret);
  }

  private void outputWithPattern(Object value, RoundingMode roundingMode, Scope scope, Env env, Writer writer) {
    Object pattern = patternExpr.eval(scope, env);
    if (!(pattern instanceof String)) {
      throw new TemplateException("The sencond parameter pattern of #number directive must be String", location);
    }

    DecimalFormat df = new DecimalFormat((String) pattern);
    df.setRoundingMode(roundingMode);

    String ret = df.format(value);
    write(writer, ret);
  }
}





