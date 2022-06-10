package use.sql;

import use.template.io.Writer;
import use.template.Directive;
import use.template.Env;
import use.template.stat.Scope;

public class WriterDirective extends Directive {
  @Override
  public void exec(Env env, Scope scope, Writer writer) {
    writer.writeVal(exprList.eval(scope, env));
  }
}
