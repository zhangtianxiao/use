

package use.template.stat;

import org.jetbrains.annotations.NotNull;
import use.beans.Beans;
import use.template.EngineConfig;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 Scope
 1：顶层 scope.parent 为 null
 2：scope.set(...) 自内向外查找赋值
 3：scope.get(...) 自内向外查找获取
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Scope {
  public static final Supplier<Scope> maker = Scope::new;
  public static final Function<Scope,Boolean> clearer = it->{
    it.clear();
    return true;
  };

  public Map data;
  public Map innerData = new HashMap();
  private Scope parent;
  private Ctrl ctrl;
  private Map<String, Object> sharedObjectMap;
  public Beans beans;

  public Scope() {
    //log.info("scope 创建: " + this.getClass() + " " + System.identityHashCode(this));
  }


  public void init(Scope parent) {
    // log.debug("scope init by parent : "+this.getClass()+" "+System.identityHashCode(this));
    this.data = innerData;
    //this.data = parent.data;

    this.ctrl = parent.ctrl;
    this.parent = parent;
    this.sharedObjectMap = parent.sharedObjectMap;
    this.beans = parent.beans;
  }

  public void init(@NotNull Map data, EngineConfig config) {
    //log.debug("scope init by config: "+this.getClass()+" "+System.identityHashCode(this));

    this.data = data;

    this.ctrl = new Ctrl();
    this.parent = null;
    this.sharedObjectMap = config.getSharedObjectMap();
    this.beans = config.beans;
  }

  public void clear() {
    if(this.innerData.size()!=0)
      this.innerData.clear();
    this.data = null;
    this.ctrl = null;
    this.parent = null;
    this.sharedObjectMap = null;
    this.beans = null;
  }

  public Ctrl getCtrl() {
    return ctrl;
  }

  /**
   设置变量
   自内向外在作用域栈中查找变量，如果找到则改写变量值，否则将变量存放到顶层 Scope
   */
  public void set(Object key, Object value) {
    for (Scope cur = this; true; cur = cur.parent) {
      // HashMap 允许有 null 值 value，必须要做 containsKey 判断
      if (cur.data.containsKey(key)) {
        cur.data.put(key, value);
        return;
      }

      if (cur.parent == null) {
        cur.data.put(key, value);
        return;
      }
    }
  }

  /**
   获取变量
   自内向外在作用域栈中查找变量，返回最先找到的变量
   */
  public <T> T get(Object key) {
    Scope cur = this;
    do {
      Object ret = cur.data.get(key);
      if (ret != null) {
        return (T) ret;
      }

      if (cur.data.containsKey(key)) {
        return null;
      }

      cur = cur.parent;
    } while (cur != null);

    // return null;
    return sharedObjectMap != null ? (T) sharedObjectMap.get(key) : null;
  }

  public BigDecimal getBigDecimal(Object key) {
    return this.get(key);
  }

  /**
   移除变量
   自内向外在作用域栈中查找变量，移除最先找到的变量
   */
  public void remove(Object key) {
    for (Scope cur = this; cur != null; cur = cur.parent) {
      if (cur.data.containsKey(key)) {
        cur.data.remove(key);
        return;
      }
    }
  }

  /**
   设置局部变量
   */
  public void setLocal(Object key, Object value) {
    data.put(key, value);
  }

  /**
   获取局部变量
   */
  public Object getLocal(Object key) {
    return data.get(key);
  }

  /**
   移除局部变量
   */
  public void removeLocal(Object key) {
    data.remove(key);
  }

  /**
   设置全局变量
   全局作用域是指本次请求的整个 template
   */
  public void setGlobal(Object key, Object value) {
    for (Scope cur = this; true; cur = cur.parent) {
      if (cur.parent == null) {
        cur.data.put(key, value);
        return;
      }
    }
  }

  /**
   获取全局变量
   全局作用域是指本次请求的整个 template
   */
  public Object getGlobal(Object key) {
    for (Scope cur = this; true; cur = cur.parent) {
      if (cur.parent == null) {
        return cur.data.get(key);
      }
    }
  }

  /**
   移除全局变量
   全局作用域是指本次请求的整个 template
   */
  public void removeGlobal(Object key) {
    for (Scope cur = this; true; cur = cur.parent) {
      if (cur.parent == null) {
        cur.data.remove(key);
        return;
      }
    }
  }

  /**
   自内向外在作用域栈中查找变量，获取变量所在的 Map，主要用于 IncDec
   */
  public Map getMapOfValue(Object key) {
    for (Scope cur = this; cur != null; cur = cur.parent) {
      if (cur.data.containsKey(key)) {
        return cur.data;
      }
    }
    return null;
  }

  /**
   获取本层作用域 data，可能为 null 值
   */
  public Map getData() {
    return data;
  }


  /**
   获取顶层作用域 data，可能为 null 值
   */
  public Map getRootData() {
    for (Scope cur = this; true; cur = cur.parent) {
      if (cur.parent == null) {
        return cur.data;
      }
    }
  }

  /**
   自内向外在作用域栈中查找变量是否存在
   */
  public boolean exists(Object key) {
    for (Scope cur = this; cur != null; cur = cur.parent) {
      if (cur.data.containsKey(key)) {
        return true;
      }
    }
    return false;
  }

  /**
   获取共享对象
   */
  public Object getSharedObject(String key) {
    return sharedObjectMap != null ? sharedObjectMap.get(key) : null;
  }
}



