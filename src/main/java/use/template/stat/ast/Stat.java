

package use.template.stat.ast;

import org.slf4j.Logger;
import use.template.io.Writer;
import use.kit.Helper;
import use.template.Env;
import use.template.expr.ast.ExprList;
import use.template.stat.Location;
import use.template.stat.Scope;

/**
 Stat */
public abstract class Stat {
  //private static final Logger log = LoggerFactory.getLogger(Stat.class);
  private static final Logger log = Helper.getLogger("engine");

  public Stat() {
    if (true) {
      //log.debug("stat创建: " + this.getClass() + " " + System.identityHashCode(this));
    }
  }

  protected Location location;

  public Stat setLocation(Location location) {
    this.location = location;
    return this;
  }

  public Location getLocation() {
    return location;
  }

  public void setExprList(ExprList exprList) {
  }

  public void setStat(Env env,Stat stat) {
  }

  public abstract void exec(Env env, Scope scope, Writer writer);

  public boolean hasEnd() {
    return false;
  }

  protected void write(Writer writer, String str) {
    writer.writeVal(str);
  }

}


