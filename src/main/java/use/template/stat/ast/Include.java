
package use.template.stat.ast;

import use.kit.ObjectPool;
import use.template.io.Writer;
import use.template.EngineConfig;
import use.template.Env;
import use.template.expr.ast.Assign;
import use.template.expr.ast.Const;
import use.template.expr.ast.Expr;
import use.template.expr.ast.ExprList;
import use.template.source.FileSource;
import use.template.source.ISource;
import use.template.stat.*;

import java.io.File;
import java.nio.file.Path;

/**
 Include

 1：父模板被缓存时，被 include 的模板会被间接缓存，无需关心缓存问题
 2：同一个模板文件被多个父模板 include，所处的背景环境不同，例如各父模板中定义的模板函数不同
 各父模板所处的相对路径不同，所以多个父模板不能共用一次 parse 出来的结果，而是在每个被include
 的地方重新 parse

 <pre>
 两种用法：
 1：只传入一个参数，参数必须是 String 常量，如果希望第一个参数是变量可以使用 #render 指令去实现
 #include("_hot.html")

 2：传入任意多个参数，除第一个参数以外的所有参数必须是赋值表达式，用于实现参数传递功能
 #include("_hot.html", title = "热门新闻", list = newsList)

 上例中传递了 title、list 两个参数，可以代替父模板中的 #set 指令传参方式
 并且此方式传入的参数只在子模板作用域有效，不会污染父模板作用域

 这种传参方式有利于将子模板模块化，例如上例的调用改成如下的参数：
 #include("_hot.html", title = "热门项目", list = projectList)
 通过这种传参方式在子模板 _hot.html 之中，完全不需要修改对于 title 与 list
 这两个变量的处理代码，就实现了对 “热门项目” 数据的渲染
 </pre>
 */
public class Include extends Stat {

  private Assign[] assignArray;
  private Stat stat;

  public Include(Env env, ExprList exprList, String parentFileName, Location location) {
    int len = exprList.length();
    if (len == 0) {
      throw new ParseException("The parameter of #include directive can not be blank", location);
    }
    // 第一个参数必须为 String 类型
    Expr expr = exprList.getExpr(0);
    if (expr instanceof Const && ((Const) expr).isStr()) {
    } else {
      throw new ParseException("The first parameter of #include directive must be String, or use the #render directive", location);
    }
    // 其它参数必须为赋值表达式
    if (len > 1) {
      for (int i = 1; i < len; i++) {
        if (!(exprList.getExpr(i) instanceof Assign)) {
          throw new ParseException("The " + (i + 1) + "th parameter of #include directive must be an assignment expression", location);
        }
      }
    }

    parseSubTemplate(env, ((Const) expr).getStr(), parentFileName, location);
    getAssignExpression(exprList);
  }

  private void parseSubTemplate(Env env, String fileName, String parentFileName, Location location) {
    String subFileName = getSubFileName(fileName, parentFileName);
    EngineConfig config = env.config;
    // FileSource fileSource = new FileSource(config.getBaseTemplatePath(), subFileName, config.encoding);
    ISource isource = config.getSource(subFileName);
    // 限定只能访问baseTemplatePath下的文件
    if (isource instanceof FileSource fileSource) {
      String baseTemplatePath = config.sourceFactory.baseTemplatePath();
      Path base = new File(baseTemplatePath).toPath().toAbsolutePath();
      Path file = fileSource.file.toPath().toAbsolutePath();
      if (file.startsWith(base)) {
      } else {
        throw new ParseException("include路径非法: " + fileName + ": " + file, location);
      }
    }
    try {
      Parser parser = new Parser(env, subFileName);
      if (config.isDevMode()) {
        env.addSource(isource);
      }
      this.stat = parser.parse(isource).getActualStat();
    } catch (Exception e) {
      // 文件路径不正确抛出异常时添加 location 信息
      throw new ParseException(e.getMessage(), location, e);
    }
  }

  /**
   获取在父模板之下子模板的最终文件名，子模板目录相对于父模板文件目录来确定
   以 "/" 打头则以 baseTemplatePath 为根，否则以父文件所在路径为根
   */
  public static String getSubFileName(String fileName, String parentFileName) {
    if (parentFileName == null) {
      return fileName;
    }
    if (fileName.startsWith("/")) {
      return fileName;
    }
    int index = parentFileName.lastIndexOf('/');
    if (index == -1) {
      return fileName;
    }
    return parentFileName.substring(0, index + 1) + fileName;
  }

  private void getAssignExpression(ExprList exprList) {
    int len = exprList.length();
    if (len > 1) {
      assignArray = new Assign[len - 1];
      for (int i = 0; i < assignArray.length; i++) {
        assignArray[i] = (Assign) exprList.getExpr(i + 1);
      }
    } else {
      assignArray = null;
    }
  }

  public void exec(Env env, Scope parent, Writer writer) {
    ObjectPool<Scope> scopePool = env.config.scopePool;
    Scope scope = scopePool.get();
    scope.init(parent);
    try {
      if (assignArray != null) {
        evalAssignExpression(scope, env);
      }
      stat.exec(env, scope, writer);
      scope.getCtrl().setJumpNone();
    } finally {
      scopePool.recycle(scope);
    }

  }

  private void evalAssignExpression(Scope scope, Env env) {
    Ctrl ctrl = scope.getCtrl();
    try {
      ctrl.setLocalAssignment();
      for (Assign assign : assignArray) {
        assign.eval(scope, env);
      }
    } finally {
      ctrl.setWisdomAssignment();
    }
  }
}







