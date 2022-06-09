

package use.kit;

import cn.hutool.core.util.ReflectUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static use.kit.Helper.as;

/**
 反射工具类
 */
public class ReflectKit {
  @Nullable
  public static <T extends Annotation, R> R mapAnnoValue(Class<?> c, Class<T> a, Function<T, R> f) {
    T an = c.getAnnotation(a);
    if (an == null)
      return null;
    return f.apply(an);
  }

  @Nullable
  public static <T extends Annotation, R> R mapAnnoValue(Method m, Class<T> a, Function<T, R> f) {
    T an = m.getAnnotation(a);
    if (an == null)
      return null;
    return f.apply(an);
  }

  //@NotNull
  public static <T extends Annotation, R> R mapAnnoValue(Class<?> c, Class<T> a, Function<T, R> f, Supplier<R> d) {
    T an = c.getAnnotation(a);
    if (an == null)
      return d.get();
    R r = f.apply(an);
    return r;
  }

  @SuppressWarnings("all")
  public static <T> T newInstance(Class<T> clazz) {
    try {
      return (T) clazz.getConstructors()[0].newInstance();
      //return clazz.newInstance();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getMethodSignature(Method method) {
    StringBuilder ret = new StringBuilder()
      .append(method.getDeclaringClass().getName())
      .append(".")
      .append(method.getName())
      .append("(");

    int index = 0;
    Parameter[] paras = method.getParameters();
    for (Parameter p : paras) {
      if (index++ > 0) {
        ret.append(", ");
      }
      ret.append(p.getParameterizedType().getTypeName());
    }

    return ret.append(")").toString();
  }

  @NotNull
  public static Field getField(Class<?> c, String field) {
    Field ret = ReflectUtil.getField(c, field);
    ret.setAccessible(true);
    return ret;
  }

  public static Object getFieldValue(Object o, Field fieldsField) {
    try {
      return fieldsField.get(o);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static final HashMap<Class<?>, Supplier> beanMakerCache = new HashMap<>(1024, 0.25F);

  public static <T> void register(Class<T> c, Supplier<T> t) {
    beanMakerCache.put(c, t);
  }

  public static <T> Supplier<T> resolveMaker(Class<T> c) {
    Supplier supplier = beanMakerCache.get(c);
    if (supplier == null) {
      Constructor<?> constructor = ReflectUtil.getConstructor(c);
      if (constructor == null || constructor.getParameterCount() != 0)
        throw new RuntimeException("仅接受无参构造");
      supplier = () -> {
        try {
          return constructor.newInstance();
        } catch (Throwable e) {
          throw new RuntimeException(e);
        }
      };
      beanMakerCache.put(c, supplier);
    }
    Objects.requireNonNull(supplier);
    return supplier;
  }

  @Nullable
  public static String getValue(Method m, Class<? extends Annotation>... a) {
    for (Class<? extends Annotation> aClass : a) {
      Annotation annotation = m.getAnnotation(aClass);
      if (annotation != null)
        return as(ReflectUtil.invoke(annotation, "value"));
    }
    return null;
  }

  @Nullable
  public static Annotation getAnnotation(Method m, Class<? extends Annotation>... a) {
    for (Class<? extends Annotation> aClass : a) {
      Annotation annotation = m.getAnnotation(aClass);
      if (annotation != null)
        return annotation;
    }
    return null;
  }

  @Nullable
  public static <T, A extends Annotation> T ifPresent(Class<?> c, Class<A> a, Function<A, T> fn) {
    A annotation = c.getAnnotation(a);
    if (annotation != null) {
      return fn.apply(annotation);
    }
    return null;
  }

  @Nullable
  public static <T, A extends Annotation> T ifPresent(java.lang.reflect.Method m, Class<A> a, Function<A, T> fn) {
    A annotation = m.getAnnotation(a);
    if (annotation != null) {
      return fn.apply(annotation);
    }
    return null;
  }

  public static Object invoke(Object o, Method method, Object[] args) {
    try {
      return method.invoke(o, args);
    } catch (Exception e) {
      if (e instanceof RuntimeException re) throw re;
      else throw new RuntimeException(e);
    }
  }

	/*
	public static String getMethodSignature(Method method) {
		StringBuilder ret = new StringBuilder()
				.append(method.getDeclaringClass().getName())
				.append(".")
				.append(method.getName())
				.append("(");

		int index = 0;
		java.lang.reflect.Type[] paraTypes = method.getGenericParameterTypes();
		for (java.lang.reflect.Type type : paraTypes) {
			if (index++ > 0) {
				ret.append(", ");
			}
			ret.append(type.getTypeName());
		}

		return ret.append(")").toString();
	}*/

  public static int hash(Method method) {
    Class<?>[] parameterTypes = method.getParameterTypes();
    Object[] arr = new Object[2 + parameterTypes.length];
    arr[0] = method.getDeclaringClass().getName();
    arr[1] = method.getName();
    for (int i = 0; i < parameterTypes.length; i++) {
      arr[2 + i] = method.getDeclaringClass().getName();
    }
    int ret = Objects.hash(arr);
    return Math.abs(ret);
  }
}






