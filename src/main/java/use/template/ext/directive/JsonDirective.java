package use.template.ext.directive;

import use.template.io.Writer;
import use.template.Directive;
import use.template.Env;
import use.template.TemplateException;
import use.template.stat.Scope;
import com.jsoniter.output.JsonStream;

import java.io.IOException;

public class JsonDirective extends Directive {
  @Override
  public void exec(Env env, Scope scope, Writer writer) {
    Object obj = exprList.eval(scope,env);
    JsonStream jsonStream = new JsonStream(writer, 1024);
    try {
      jsonStream.writeVal(obj);
      jsonStream.close();
    } catch (IOException e) {
      throw new TemplateException(e.getMessage(), location, e);
    }
  }
}
