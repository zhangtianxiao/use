package use.mvc.pararesolver;

import use.mvc.router.Action;
import use.mvc.parabind.PathVariable;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.function.Function;

class ParaResolver_PathVariable extends ParaResolver {
  public static final ParaResolver_PathVariable me = new ParaResolver_PathVariable();
  final Function<String, Object> converter;

  private ParaResolver_PathVariable() {
    super(null, null);
    converter = null;
  }

  protected ParaResolver_PathVariable(String name, Parameter p, Type type) {
    super(name, p.getAnnotation(PathVariable.class).value());
    converter = ParaResolver_Query.match(type);
  }

  @Override
  public Object resolve(Action action) {
    String value = action.path_variable(name);
    if (value == null) return null;
    Object apply = converter.apply(value);
    return apply;
  }

  @Override
  public ParaResolver resolve(Parameter p, Type type, int i) {
    return new ParaResolver_PathVariable(p.getName(), p, type);
  }

  @Override
  public boolean match(Parameter p, Type type, int i) {
    return p.isAnnotationPresent(PathVariable.class);
  }
}
