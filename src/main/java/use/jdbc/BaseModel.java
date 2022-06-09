package use.jdbc;

import com.jsoniter.any.Any;
import use.kit.ex.Unsupported;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseModel<T extends BaseModel<T>> {
  protected abstract Long getId();

  // protected 而不是 public, 是为了让idea自动过滤掉不符合包权限的自动提示
  protected abstract String schema();

  protected abstract String table();

  public transient Map<String, Object> attrs = null;

  protected final void checkAttrs() {
    if (attrs == null) attrs = new LinkedHashMap<>();
  }
  
  public void bind(Any any) {
    any.bindTo(this);
  }

  public void bind(Any any, Object... keys) {
    any.bindTo(this, keys);
  }

  public int update(Db db) {
    if (attrs != null) {
      Long id = getId();
      if (id != null) {
        int update = db.update(this, id);
        this.attrs.clear();
        return update;
      }
    }
    return 0;
  }

  public int save(Db db) {
    if (attrs != null) {
      int save = db.save(this);
      this.attrs.clear();
      return save;
    }
    return 0;
  }

  public int delete(Db db) {
    if (attrs != null) {
      Long id = getId();
      if (id != null) db.delete(this, id);
    }
    return 0;
  }

  @Override
  public int hashCode() {
    throw new Unsupported("请自行按需实现");
  }

  @Override
  public String toString() {
//    return JsonStream.serialize(this);
    return null;
  }

}
