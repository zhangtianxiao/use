

package use.template.ext.directive;

import use.template.io.Writer;
import use.template.Directive;
import use.template.Env;
import use.template.stat.Scope;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 输出 int 型随机数
 */
public class RandomDirective extends Directive {

  public void exec(Env env, Scope scope, Writer writer) {
    writer.writeVal(ThreadLocalRandom.current().nextInt());
  }
}




