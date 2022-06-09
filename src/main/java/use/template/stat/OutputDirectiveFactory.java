

package use.template.stat;

import use.template.expr.ast.ExprList;
import use.template.stat.ast.Output;

public class OutputDirectiveFactory {

  public static final OutputDirectiveFactory me = new OutputDirectiveFactory();

  public Output getOutputDirective(ExprList exprList, Location location) {
    return new Output(exprList, location);
  }
}


