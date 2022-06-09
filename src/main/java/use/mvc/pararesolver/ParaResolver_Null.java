package use.mvc.pararesolver;

import use.mvc.router.Action;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

class ParaResolver_Null extends ParaResolver {
  public static final ParaResolver_Null me = new ParaResolver_Null();

  ParaResolver_Null() {
    super(null);
  }

  @Override
  public Object resolve(Action action) {
    return null;
  }

  @Override
  public ParaResolver resolve(Parameter p, Type type, int i) {
    return me;
  }

  @Override
  public boolean match(Parameter p, Type type, int i) {
    return true;
  }
}
