package use.mvc.pararesolver;

import use.mvc.router.Action;
import use.mvc.parabind.CookieValue;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.function.Function;

class ParaResolver_Cookie extends ParaResolver {
  public static final ParaResolver_Cookie me = new ParaResolver_Cookie();
  final Function<String, Object> converter;

  private ParaResolver_Cookie() {
    super(null, null);
    converter = null;
  }

  protected ParaResolver_Cookie(Parameter p, Type type) {
    super(p.getName(), p.getAnnotation(CookieValue.class).value());
    converter = ParaResolver_Query.match(type);
  }

  @Override
  public Object resolve(Action action) {
    String value = action.cookie(name);
    if (value == null) return null;
    Object apply = converter.apply(value);
    return apply;
  }

  @Override
  public ParaResolver resolve(Parameter p, Type type, int i) {
    return new ParaResolver_Cookie(p, type);
  }

  @Override
  public boolean match(Parameter p, Type type, int i) {
    return p.isAnnotationPresent(CookieValue.class);
  }
}
