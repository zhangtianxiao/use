package use.mvc.pararesolver;

import use.mvc.router.Action;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.function.Function;

class ParaResolver_Query extends ParaResolver {
  public static final ParaResolver_Query me = new ParaResolver_Query();
  final Function<String, Object> converter;

  ParaResolver_Query() {
    super(null);
    converter = null;
  }

  ParaResolver_Query(Parameter p, Type type, int i) {
    super(p.getName());
    converter = match(type);
  }

  static Function<String, Object> match(Type type) {
    Function<String, Object> converter;
    if (type == String.class)
      converter = value -> value;
    else if (type == Integer.class || type == int.class)
      converter = Integer::parseInt;
    else if (type == Short.class || type == short.class)
      converter = Short::parseShort;
    else if (type == Long.class || type == long.class)
      converter = Long::parseLong;
    else if (type == Double.class || type == double.class)
      converter = Double::parseDouble;
    else if (type == Float.class || type == float.class)
      converter = Float::parseFloat;
    else if (type == BigDecimal.class)
      converter = BigDecimal::new;
    else if (type == Boolean.class || type == boolean.class)
      converter = Boolean::parseBoolean;
    else
      converter = value -> null;
    return converter;
  }

  @Override
  public Object resolve(Action action) {
    String value = action.param(name);
    if (value == null) return null;
    Object apply = converter.apply(value);
    return apply;
  }

  @Override
  public ParaResolver resolve(Parameter p, Type type, int i) {
    return new ParaResolver_Query(p, type, i);
  }

  @Override
  public boolean match(Parameter p, Type type, int i) {
    return type == String.class || type == Integer.class || type == int.class
      || type == Long.class || type == long.class
      || type == Short.class || type == short.class
      || type == Double.class || type == double.class
      || type == Float.class || type == float.class
      || type == BigDecimal.class
      || type == Boolean.class || type == boolean.class
      ;
  }
}
