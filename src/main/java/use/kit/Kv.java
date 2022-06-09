

package use.kit;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Kv (Key Value)
 *
 * Example：
 *    Kv para = Kv.by("id", 123);
 *    User user = user.findFirst(getSqlPara("find", para));
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Kv extends HashMap {
  public static final Supplier<Kv> maker = Kv::new;

  private static final long serialVersionUID = -808251639784763326L;

  public Kv() {
  }

  public static Kv of(Object key, Object value) {
    return new Kv().set(key, value);
  }

  public static Kv by(Object key, Object value) {
    return new Kv().set(key, value);
  }

  public static Kv create() {
    return new Kv();
  }

  public Kv set(Object key, Object value) {
    super.put(key, value);
    return this;
  }

  public Kv setIfNotBlank(Object key, String value) {
    if (StrKit.notBlank(value)) {
      set(key, value);
    }
    return this;
  }

  public Kv setIfNotNull(Object key, Object value) {
    if (value != null) {
      set(key, value);
    }
    return this;
  }

  public Kv set(Map map) {
    super.putAll(map);
    return this;
  }

  public Kv set(Kv kv) {
    super.putAll(kv);
    return this;
  }

  public Kv delete(Object key) {
    super.remove(key);
    return this;
  }

  public <T> T getAs(Object key) {
    return (T) get(key);
  }

  public String getStr(Object key) {
    Object s = get(key);
    return s != null ? s.toString() : null;
  }

  public BigDecimal getBigDecimal(Object key) {
    return (BigDecimal)get(key);
  }

  public Integer getInt(Object key) {
    Number n = (Number) get(key);
    return n != null ? n.intValue() : null;
  }

  public Long getLong(Object key) {
    Number n = (Number) get(key);
    return n != null ? n.longValue() : null;
  }

  public Double getDouble(Object key) {
    Number n = (Number) get(key);
    return n != null ? n.doubleValue() : null;
  }

  public Float getFloat(Object key) {
    Number n = (Number) get(key);
    return n != null ? n.floatValue() : null;
  }

  public Number getNumber(Object key) {
    return (Number) get(key);
  }

  public Boolean getBoolean(Object key) {
    return (Boolean) get(key);
  }

  /**
   * key 存在，并且 value 不为 null
   */
  public boolean notNull(Object key) {
    return get(key) != null;
  }

  /**
   * key 不存在，或者 key 存在但 value 为null
   */
  public boolean isNull(Object key) {
    return get(key) == null;
  }

  /**
   * key 存在，并且 value 为 true，则返回 true
   */
  public boolean isTrue(Object key) {
    Object value = get(key);
    return (value instanceof Boolean && ((Boolean) value == true));
  }

  /**
   * key 存在，并且 value 为 false，则返回 true
   */
  public boolean isFalse(Object key) {
    Object value = get(key);
    return (value instanceof Boolean && ((Boolean) value == false));
  }

  public boolean equals(Object kv) {
    return kv instanceof Kv && super.equals(kv);
  }

  public Kv keep(String... keys) {
    if (keys != null && keys.length > 0) {
      Kv newKv = Kv.create();
      for (String k : keys) {
        if (containsKey(k)) {  // 避免将并不存在的变量存为 null
          newKv.put(k, get(k));
        }
      }

      clear();
      putAll(newKv);
    } else {
      clear();
    }

    return this;
  }

  interface Itf{
    default void fn(BiConsumer<Class<?>,Object> c){
      c.accept(Itf.class,this);
    }
  }

  static class Impl implements Itf{
    public Impl(){
      this.fn((c,o)->{
        //
      });
    }
  }


}



