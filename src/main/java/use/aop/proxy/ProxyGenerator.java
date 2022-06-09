

package use.aop.proxy;

import org.slf4j.Logger;
import use.aop.Before;
import use.aop.Clear;
import use.kit.BeanFactory;
import use.kit.Helper;
import use.kit.Kv;
import use.kit.ReflectKit;
import use.mvc.mi.MappingInfo;
import use.template.Engine;
import use.template.Template;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static use.kit.Helper.as;


public class ProxyGenerator {
  protected static final Logger log = Helper.getLogger(ProxyGenerator.class);

  public static final ProxyGenerator me = new ProxyGenerator();

  private ProxyGenerator() {
  }

  protected Engine engine = Engine.byClassPath("forProxy");
  protected Template template = engine.getTemplate("use/aop/proxy/proxy_class_template.jf");

  protected boolean printGeneratedClassToConsole = false;

  protected List<Kv> buildParaTypes(Parameter[] paras) {
    return Arrays.asList(paras).stream().map(
      x -> {
        // 参考 JDK Parameter
        Kv kv = new Kv();
        Class<?> type = as(x.getParameterizedType());
        String typename = type.getCanonicalName();
        String typeStr = x.isVarArgs() ? typename.replaceFirst("\\[\\]$", "...") : typename;
        String name = x.getName();
        kv.put("name", name);
        kv.put("type", typeStr);

        return kv;
      }
    ).collect(Collectors.toList());
  }

  public static Method[] methods(Class<?> target) {
    return target.getDeclaredMethods();
  }

  public boolean needProxy(Class<?> target) {
    List<Class<?>> methodUpperInters = getMethodUpperInterceptors(target);
    Method[] methodArray = methods(target);
    for (Method m : methodArray) {
      if (isSkipMethod(m)) {
        continue;
      }
      // 没有拦截器的 method 不生成 ProxyMethod
      if (!hasInterceptor(methodUpperInters, m)) {
        continue;
      }
      return true;
    }
    return false;
  }

  public ProxyClass generate(Class<?> target, BeanFactory beanFactory) {
    ProxyClass proxyClass = new ProxyClass(target);

    Kv clazz = Kv.create();
    String targetName = getTargetName(target);
    clazz.set("pkg", proxyClass.getPkg());
    clazz.set("name", proxyClass.getName());
    clazz.set("targetName", targetName);

    @SuppressWarnings("rawtypes")
    TypeVariable[] tvs = target.getTypeParameters();
    clazz.set("classTypeVars", getTypeVars(tvs));
    clazz.set("targetTypeVars", getTargetTypeVars(tvs));

    List<Class<?>> methodUpperInters = getMethodUpperInterceptors(target);

    Constructor<?>[] constructors = target.getConstructors();
    Constructor<?> constructor = constructors[0];
    clazz.set("constructorParaTypes", buildParaTypes(constructor.getParameters()));
    clazz.set("throws", getThrows(constructor));

    List<Kv> methodList = new ArrayList<>();
    clazz.set("methodList", methodList);
    // 只获取本类的方法
    Method[] methodArray = methods(target);
    for (Method m : methodArray) {
      if (isSkipMethod(m)) {
        continue;
      }

      // 没有拦截器的 method 不生成 ProxyMethod
      if (!hasInterceptor(methodUpperInters, m)) {
        continue;
      }
      int hash = ReflectKit.hash(m);
      Kv method = Kv.create();
      method.set("methodTypeVars", getTypeVars(m.getTypeParameters()));
      method.set("returnType", getReturnType(m));
      method.set("name", m.getName());
      method.set("throws", getThrows(m));
      method.set("hash", hash);


      Parameter[] paras = m.getParameters();

      List<Kv> paraTypes = buildParaTypes(paras);
      method.set("paraTypes", paraTypes);
      /**
       对内置mvc的支持
       */
      method.put("mvcActon", MappingInfo.canMapping(m));

      // 只有一个参数，且该参数是数组或者可变参数时传递 singleArrayPara = true
      if (paras.length == 1) {
        if (paras[0].getType().isArray() || paras[0].isVarArgs()) {
          method.set("singleArrayPara", true);
        }
      }

      if (m.getReturnType() != void.class) {
        method.set("frontReturn", "return ");
      } else {
        method.set("backReturn", "return null;");
      }

      methodList.add(method);

      ProxyMethod proxyMethod = new ProxyMethod(hash, m, target, beanFactory);
      proxyClass.addProxyMethod(proxyMethod);
    }

    if (proxyClass.needProxy()) {
      String sourceCode = template.renderToString(clazz);
      proxyClass.setSourceCode(sourceCode);

      if (printGeneratedClassToConsole) {
        String msg = "Generate proxy class \"" + proxyClass.getPkg() + "." + proxyClass.getName() + "\":";
        System.out.println(msg);
        System.out.println(sourceCode);
      }
      /*if (log.isDebugEnabled()) {
        String msg = "\nGenerate proxy class \"" + proxyClass.getPkg() + "." + proxyClass.getName() + "\":";
        log.debug(msg + sourceCode);
      }*/
    }

    return proxyClass;
  }

  /**
   支持对 static 类的代理
   */
  protected String getTargetName(Class<?> target) {
    if (Modifier.isStatic(target.getModifiers())) {
      // 无法兼容主类类名中包含字符 '$'，例如：com.xxx.My$Target&Inner
      // return target.getName().replace('$', '.');

      // 静态类的 getName() 值为 com.xxx.Target&Inner 需要将字符 '$' 替换成 '.'
      String ret = target.getName();
      int index = ret.lastIndexOf('$');
      return ret.substring(0, index) + "." + ret.substring(index + 1);
    } else {
      return target.getSimpleName();
    }
  }

  /**
   方法返回值为 int[] 时 method.getReturnType().getName() 返回值为: [I
   需要识别并转化
   */
  protected String getReturnType(Method method) {
    // return method.getReturnType().getName();
    // return method.getAnnotatedReturnType().getType().getTypeName();
    return method.getGenericReturnType().getTypeName();
  }

  /**
   获取子类泛型变量，也可用于获取方法泛型变量
   */
  @SuppressWarnings("rawtypes")
  protected String getTypeVars(TypeVariable[] typeVars) {
    if (typeVars == null || typeVars.length == 0) {
      return null;
    }

    StringBuilder ret = new StringBuilder();

    ret.append('<');
    for (int i = 0; i < typeVars.length; i++) {
      TypeVariable tv = typeVars[i];
      if (i > 0) {
        ret.append(", ");
      }

      ret.append(tv.getName());

      // T extends Map & List & Set
      Type[] bounds = tv.getBounds();
      if (bounds.length == 1) {
        if (bounds[0] != Object.class) {
          ret.append(" extends ").append(bounds[0].getTypeName());
          continue;
        }
      } else {
        for (int j = 0; j < bounds.length; j++) {
          String tn = bounds[j].getTypeName();
          if (j > 0) {
            ret.append(" & ").append(tn);
          } else {
            ret.append(" extends ").append(tn);
          }
        }
      }
    }

    return ret.append('>').toString();
  }

  /**
   获取父类泛型变量

   相对于 getTypeVars(...) 取消了 TypeVariable.getBounds() 内容的生成，否则编译错误
   */
  @SuppressWarnings("rawtypes")
  protected String getTargetTypeVars(TypeVariable[] typeVars) {
    if (typeVars == null || typeVars.length == 0) {
      return null;
    }

    StringBuilder ret = new StringBuilder();
    ret.append('<');
    for (int i = 0; i < typeVars.length; i++) {
      TypeVariable tv = typeVars[i];
      if (i > 0) {
        ret.append(", ");
      }
      ret.append(tv.getName());
    }
    return ret.append('>').toString();
  }

  /**
   获取方法抛出的异常
   */
  protected String getThrows(Executable method) {
    Class<?>[] throwTypes = method.getExceptionTypes();
    if (throwTypes == null || throwTypes.length == 0) {
      return null;
    }

    StringBuilder ret = new StringBuilder().append("throws ");
    for (int i = 0; i < throwTypes.length; i++) {
      if (i > 0) {
        ret.append(", ");
      }
      ret.append(throwTypes[i].getName());
    }
    return ret.append(' ').toString();
  }

  /**
   跳过不能代理的方法
   1：非 public
   2：final、static、abstract
   3：方法名为：toString、hashCode、equals
   */
  protected boolean isSkipMethod(Method method) {
    int mod = method.getModifiers();
    if (!Modifier.isPublic(mod)) {
      return true;
    }

    if (Modifier.isFinal(mod) || Modifier.isStatic(mod) || Modifier.isAbstract(mod)) {
      return true;
    }

    String n = method.getName();
    if (n.equals("toString") || n.equals("hashCode") || n.equals("equals")) {
      return true;
    }

    return false;
  }

  /**
   获取 method 上层的拦截器，也即获取 global、class 这两层拦截器
   注意：global 层拦截器已结合 class 层 @Clear 注解处理过
   */
  protected List<Class<?>> getMethodUpperInterceptors(Class<?> target) {
    List<Class<?>> ret = new ArrayList<>();
    // 追加 class 级拦截器
    Before beforeOnClass = target.getAnnotation(Before.class);
    if (beforeOnClass != null) {
      Class<?>[] classInters = beforeOnClass.value();
      for (Class<?> c : classInters) {
        ret.add(c);
      }
    }

    return ret;
  }

  protected void removeInterceptor(List<Class<?>> target, Class<?>[] clearInters) {
    if (target.isEmpty() || clearInters.length == 0) {
      return;
    }

    for (Iterator<Class<?>> it = target.iterator(); it.hasNext(); ) {
      Class<?> interClass = it.next();
      for (Class<?> c : clearInters) {
        if (c == interClass) {
          it.remove();
          break;
        }
      }
    }
  }

  /**
   当前 method 是否存在有效拦截器
   1：如果存在 method 级拦截器，则 return true
   2：否则结合 method 级的 @Clear 考察 global、class 两层拦截器的留存
   global、class 两层拦截器已作为参数 methodUpperInters 被传入
   methodUpperInters 中的拦截器已结合 class 级 @Clear 处理过
   */
  protected boolean hasInterceptor(List<Class<?>> methodUpperInters, Method method) {
    // 如果 method 存在拦截器，可断定 hasInterceptor 为真，因为 @Clear 不能清除 method 级拦截器
    Before beforeOnMethod = method.getAnnotation(Before.class);
    if (beforeOnMethod != null && beforeOnMethod.value().length != 0) {
      return true;
    }

    // method 级拦截器不存在的情况，只需考虑察 global、class 级拦截器结合 method 级 @Clear 的留存情况
    List<Class<?>> ret;
    Clear clearOnMethod = method.getAnnotation(Clear.class);
    if (clearOnMethod != null) {
      Class<?>[] clearIntersOnMethod = clearOnMethod.value();
      if (clearIntersOnMethod.length != 0) {
        // 复制一份 methodUpperInters，以免 removeInterceptorClass 操作影响下次迭代
        ret = copyInterceptors(methodUpperInters);
        removeInterceptor(ret, clearIntersOnMethod);
      } else {
        ret = null;
      }
    } else {
      ret = methodUpperInters;
    }

    return ret != null && ret.size() > 0;
  }

  protected List<Class<?>> copyInterceptors(List<Class<?>> methodUpperInters) {
    List<Class<?>> ret = new ArrayList<>(methodUpperInters.size());
    for (Class<?> c : methodUpperInters) {
      ret.add(c);
    }
    return ret;
  }

  /**
   配置打印生成类到控制台
   */
  public void setPrintGeneratedClassToConsole(boolean printGeneratedClassToConsole) {
    this.printGeneratedClassToConsole = printGeneratedClassToConsole;
  }

  public void setProxyClassTemplate(String proxyClassTemplate) {
    template = engine.getTemplate(proxyClassTemplate);
  }
}








