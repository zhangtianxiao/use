
package use.template.expr.ast;

import use.template.Env;
import use.template.TemplateException;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

/**
 StaticMethod : ID_list : '::' ID '(' exprList? ')'
 用法： use.kit.Str::isBlank("abc")
 */
public class StaticMethod extends Expr {

  private Class<?> clazz;
  private String methodName;
  private ExprList exprList;

  public StaticMethod(String className, String methodName, Location location) {
    init(className, methodName, ExprList.NULL_EXPR_LIST, location);
  }

  public StaticMethod(String className, String methodName, ExprList exprList, Location location) {
    if (exprList == null || exprList.length() == 0) {
      throw new ParseException("exprList can not be blank", location);
    }
    init(className, methodName, exprList, location);
  }

  private void init(String className, String methodName, ExprList exprList, Location location) {
    try {
      this.clazz = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new ParseException("Class not found: " + className, location, e);
    } catch (Exception e) {
      throw new ParseException(e.getMessage(), location, e);
    }

    if (MethodKit.isForbiddenClass(this.clazz)) {
      throw new ParseException("Forbidden class: " + this.clazz.getName(), location);
    }
    if (MethodKit.isForbiddenMethod(methodName)) {
      throw new ParseException("Forbidden method: " + methodName, location);
    }

    this.methodName = methodName;
    this.exprList = exprList;
    this.location = location;
  }

  @Override
  public Object eval(Scope scope, Env env) {
    Object[] argValues = exprList.evalExprList(scope, env);

    try {
      MethodInfo methodInfo = MethodKit.getMethod(clazz, methodName, argValues);

      if (methodInfo.notNull()) {
        if (methodInfo.isStatic()) {
          return methodInfo.invoke(null, argValues);
        } else {
          throw new TemplateException(Method.buildMethodNotFoundSignature("Not public static method: " + clazz.getName() + "::", methodName, argValues), location);
        }
      } else {
        // StaticMethod 是固定的存在，不支持 null safe，null safe 只支持具有动态特征的用法
        throw new TemplateException(Method.buildMethodNotFoundSignature("public static method not found: " + clazz.getName() + "::", methodName, argValues), location);
      }

    } catch (TemplateException | ParseException e) {
      throw e;
    } catch (Exception e) {
      throw new TemplateException(e.getMessage(), location, e);
    }
  }
}




