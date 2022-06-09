

package use.aop;

import use.kit.ObjectPool;
import use.kit.SimpleArray;
import use.kit.ex.Unsupported;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 aop模块本身是独立的, 不应该和mvc模块依赖关系
 目前只是图省事, 可扩展的干净做法是在ProxyGenerator中定义Invocation子类策略
 */

public class Invocation {
  public final SimpleArray args = new SimpleArray(3);

  public Object target;
  public Method method;
  private Callback callSuper;
  private Interceptor[] inters;
  protected Object returnValue;

  private int index = 0;
  public boolean canceled = false;

  public static final Supplier<Invocation> maker = Invocation::new;
  public static final Function<Invocation, Boolean> clearer = it -> {
    boolean needRecycle = it.method != null;
    if (needRecycle)
      it.clear();
    return needRecycle;
  };
  public static final ObjectPool<Invocation> pool = ObjectPool.create("aop-invocation", maker, clearer, -1);

  public void setCallSuper(Callback callSuper) {
    this.callSuper = callSuper;
  }

  public void init(Object target, Method method, Interceptor[] inters, Callback callSuper) {
    this.target = target;
    this.method = method;
    this.inters = inters;
    this.callSuper = callSuper;
  }

  public void clear() {
    this.target = null;
    this.method = null;
    this.args.clear();
    this.callSuper = null;
    this.inters = null;
    this.returnValue = null;
    this.index = 0;
    this.canceled = false;
  }

  private Invocation() {
  }

  public boolean handled() {
    return index > inters.length;
  }

  public void invoke() {
    if (canceled)
      throw new Unsupported("已取消的调用...");

    if (index < inters.length) {
      inters[index++].intercept(this);
    } else if (index++ == inters.length) {  // index++ ensure invoke action only one time
      try {
        returnValue = callSuper.invoke(this);
      } catch (InvocationTargetException e) {
        Throwable t = e.getTargetException();
        if (t == null) {
          t = e;
        }
        throw t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
      } catch (RuntimeException e) {
        throw e;
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  public Method getMethod() {
    return method;
  }

  public <T> T getReturnValue() {
    return (T) returnValue;
  }

  public void setReturnValue(Object returnValue) {
    this.returnValue = returnValue;
  }

}
