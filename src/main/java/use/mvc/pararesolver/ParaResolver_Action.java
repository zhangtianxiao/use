package use.mvc.pararesolver;

import use.mvc.router.Action;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

class ParaResolver_Action extends ParaResolver {
  public static final ParaResolver_Action me = new ParaResolver_Action();

  ParaResolver_Action() {
    super(null);
  }

  @Override
  public Object resolve(Action action) {
    return action;
  }

  @Override
  public ParaResolver resolve(Parameter p, Type type, int i) {
    return me;
  }

  @Override
  public boolean match(Parameter p, Type type, int i) {
    return type == Action.class;
  }
}
