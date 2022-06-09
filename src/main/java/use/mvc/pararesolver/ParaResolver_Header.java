package use.mvc.pararesolver;

import use.mvc.router.Action;
import use.mvc.parabind.HeaderValue;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.function.Function;

class ParaResolver_Header extends ParaResolver {
  public static final ParaResolver_Header me = new ParaResolver_Header();
  final Function<String, Object> converter;

  private ParaResolver_Header() {
    super(null, null);
    converter = null;
  }

  protected ParaResolver_Header(String name, Parameter p, Type type) {
    super(name, p.getAnnotation(HeaderValue.class).value());
    converter = ParaResolver_Query.match(type);
  }

  @Override
  public Object resolve(Action action) {
    String value = action.header(name);
    if (value == null) return null;
    Object apply = converter.apply(value);
    return apply;
  }

  @Override
  public ParaResolver resolve(Parameter p, Type type, int i) {
    return ParaResolver_Query.me.resolve(p, type, i);
  }

  @Override
  public boolean match(Parameter p, Type type, int i) {
    return p.isAnnotationPresent(HeaderValue.class);
  }
}
