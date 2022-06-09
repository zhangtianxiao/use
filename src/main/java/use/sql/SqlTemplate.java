package use.sql;

import org.jetbrains.annotations.NotNull;
import use.template.Env;
import use.template.Template;
import use.template.stat.ast.Output;
import use.template.stat.ast.Stat;
import use.template.stat.ast.StatList;
import use.template.stat.ast.Text;

import java.util.Collections;
import java.util.Map;

import static use.kit.Helper.as;

public class SqlTemplate extends Template {
  public SqlTemplate(Env env, Stat ast, SQL_TYPE sql_type, boolean isReturning) {
    this.init(env, ast, sql_type, isReturning);
  }

  SQL_TYPE sql_type;
  boolean isReturning;
  boolean isImmutable = true;
  String sql = "";

  public void init(Env env, Stat ast, SQL_TYPE sql_type, boolean isReturning) {
    super.init(env, ast);
    this.sql_type = sql_type;
    this.isReturning = isReturning;

    //  只有text节点/output节点
    StatList statList = as(ast);
    for (Stat stat : statList.statArray) {
      if (stat instanceof Text || stat instanceof Output) {
      } else {
        isImmutable = false;
        break;
      }
    }
    if (isImmutable) {
      sql = super.renderToString(Collections.emptyMap());
    }
  }

  @Override
  public boolean immutable() {
    return isImmutable;
  }

  @Override
  public String renderToString(@NotNull Map<?, ?> data) {
    if (this.isImmutable)
      return sql;
    return super.renderToString(data);
  }



}
