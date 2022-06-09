package use.beans;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.util.collection.LongObjectHashMap;
import use.aop.proxy.ProxyClass;
import use.aop.proxy.ProxyClassLoader;
import use.aop.proxy.ProxyCompiler;
import use.kit.ex.Unsupported;
import use.template.stat.ast.ForIteratorStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

import static use.kit.Helper.as;

@SuppressWarnings("all")
public class Beans {
  @SuppressWarnings("all")
  private static final Function function_new_list = k -> new ArrayList<>();
  public static Beans me = new Beans();

  public static void init(Beans me) {
    // 模板内置循环对象
    me.register(
      FieldDesc.create(ForIteratorStatus.class, "outer", (o, value) -> {
        throw new Unsupported();
      }, o -> {
        return o.getOuter();
      })
      , FieldDesc.create(ForIteratorStatus.class, "index", (o, value) -> {
        throw new Unsupported();
      }, o -> {
        return o.getIndex();
      })
      , FieldDesc.create(ForIteratorStatus.class, "size", (o, value) -> {
        throw new Unsupported();
      }, o -> {
        return o.getSize();
      })
      , FieldDesc.create(ForIteratorStatus.class, "first", (o, value) -> {
        throw new Unsupported();
      }, o -> {
        return o.getFirst();
      })
      , FieldDesc.create(ForIteratorStatus.class, "last", (o, value) -> {
        throw new Unsupported();
      }, o -> {
        return o.getLast();
      })
      , FieldDesc.create(ForIteratorStatus.class, "odd", (o, value) -> {
        throw new Unsupported();
      }, o -> {
        return o.getOdd();
      })
      , FieldDesc.create(ForIteratorStatus.class, "even", (o, value) -> {
        throw new Unsupported();
      }, o -> {
        return o.getEven();
      })
    );
  }

  public Beans() {
    init(this);
  }

  final Map<Class<?>, List<FieldDesc>> DEFAULT_CACHE = new HashMap<>();
  final Map<Long, FieldDesc> DESC_CACHE = new LongObjectHashMap<>();


  public <T> void register(Class<T> c, String name, FieldGetter<T> getter, FieldSetter<T> setter) {
    register(new FieldDesc<T>(c, name, setter, getter));
  }

  public <T> void register(Class<T> c, String name, FieldSetter<T> setter, FieldGetter<T> getter) {
    register(new FieldDesc<T>(c, name, setter, getter));
  }


  public <T> void register(FieldDesc<T>... fieldDescList) {
    Class<?> c = fieldDescList[0].c;
    List<FieldDesc> list = DEFAULT_CACHE.computeIfAbsent(c, function_new_list);
    list.addAll(Arrays.asList(fieldDescList));
    for (FieldDesc fieldDesc : fieldDescList) {
      long key = FieldKeyBuilder.me.build(c.getName(), fieldDesc.name);
      FieldDesc old = DESC_CACHE.put(key, fieldDesc);
      // 在注册时就发现hash冲突
      // 若发现hash冲突, 就应该改变hash算法, 或设计为更严格的匹配方式
      if (old != null) {
        throw new Unsupported("极低概率的冲突: Beans#register, " + c + "#" + fieldDesc.name + " old: " + old.c + "#" + old.name);
      }
    }
  }

  public Map<String, Object> merge(Object o) {
    return merge(o, new HashMap());
  }

  public Map<String, Object> merge(Object o, Map map) {
    List<FieldDesc> fieldDescs = DEFAULT_CACHE.get(o);
    if (fieldDescs == null) throw new Unsupported();
    for (FieldDesc fieldDesc : fieldDescs)
      map.put(fieldDesc.name, fieldDesc.getter.get(o));
    return map;
  }

  public <T> FieldDesc<T> desc(T o, String name) {
    Class aClass = o.getClass();
    return desc(aClass, name);
  }

  public <T> FieldDesc<T> desc(Class<T> c, String name) {
    long key = FieldKeyBuilder.me.build(c.getName(), name);
    FieldDesc<T> fieldDesc = DESC_CACHE.get(key);
    return fieldDesc;
  }

  public <T> FieldDesc<T> desc(T o, long hash) {
    long key = FieldKeyBuilder.me.build(o.getClass().getName(), hash);
    FieldDesc<T> fieldDesc = DESC_CACHE.get(key);
    return fieldDesc;
  }

  public <T> T get(Object o, String name) {
    return (T) desc(o, name).getter.get(o);
  }

  public void set(Object o, String name, Object value) {
    desc(o, name).setter.set(o, value);
  }

  public static void main(String[] args) {
  }

  public static BeansRegister auto(Class<?>... cs) {
    long l = System.currentTimeMillis();
    String className = "BeansRegister" + l;
    StringBuilder sb = new StringBuilder("package use.beans;\n" +
      "\n" +
      "public class " + className + "  implements BeansRegister{\n" +
      "  public void register(Beans beans){\n");

    for (Class<?> c : cs)
      auto(c, sb);
    sb.append("  }\n}");
    String sourceCode = sb.toString();
    System.out.println(sourceCode);
    ProxyClass proxyClass = new ProxyClass(sourceCode, "use.beans", className);
    ProxyCompiler.me.compile(proxyClass);
    ProxyClassLoader.me.loadProxyClass(proxyClass);
    BeansRegister o = as(ReflectUtil.newInstance(proxyClass.getClazz()));
    return o;
  }

  static void auto(Class<?> c, StringBuilder sb) {
    HashSet<String> names = new HashSet<>();
    Method[] methods = ReflectUtil.getMethods(c);
    Field[] fields = ReflectUtil.getFields(c);
    // 优先 public 字段
    for (Field field : fields) {
      String fieldName = field.getName();
      if (Modifier.isPublic(field.getModifiers())) {
        field.setAccessible(true);
        sb.append("\tbeans.register(FieldDesc.create(").append(c.getName()).append(".class, \"").append(fieldName)
          .append("\", (it, value) -> it.").append(fieldName).append(" = (").append(field.getType().getTypeName()).append(") value, it -> it.").append(fieldName).append("));\n");
        names.add(fieldName);
      }
    }
    //
    for (Method method : methods) {
      int modifiers = method.getModifiers();
      // 忽略静态方法和非public方法
      if (Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
      } else {
        String methodName = method.getName();
        // 忽略短名方法
        if (methodName.length() <= 3) {
        } else {
          if (methodName.startsWith("get") || methodName.startsWith("set")) {
            String sub = methodName.substring(3);
            // 忽略getClass
            if (sub.equals("Class"))
              continue;
            String fieldName = StrUtil.lowerFirst(sub);
            // 跳过public字段 和 已经识别到的get set
            if (names.contains(fieldName))
              continue;

            Method getMethod = ReflectUtil.getMethod(c, "get" + sub);
            Method setMethod = ReflectUtil.getMethod(c, "set" + sub, method.getParameterTypes());
            if (getMethod != null && getMethod.getParameterTypes().length != 0)
              getMethod = null;
            if (setMethod != null && setMethod.getParameterTypes().length != 1)
              setMethod = null;

            sb.append("\tbeans.register(FieldDesc.create(").append(c.getName()).append(".class, \"").append(fieldName).append("\",");
            if (setMethod != null) {
              Class<?> type = setMethod.getParameterTypes()[0];
              sb.append("(it, value) -> it.set").append(sub).append("(").append("(").append(type).append(")").append("value),");
            } else
              sb.append("null,");
            // 可读
            if (getMethod != null) {
              sb.append("it -> it.get").append(sub).append("()));");
            } else {
              sb.append("null));");
            }
            sb.append("\n");
            names.add(fieldName);
          }
        }
      }
    }
  }
}
