package use.jdbc.graph;

import cn.hutool.core.util.ReflectUtil;
import use.jdbc.BaseModel;
import use.jdbc.Db;

import java.util.Arrays;

public abstract class GraphModel<T extends GraphModel<T>> extends BaseModel<T> {
  transient ClassInfo<T> _classInfo;
  transient Db _db;
  transient final boolean[] _loaded;

  protected static int refFieldCount(Class<?> c) {
    return (int) Arrays.stream(ReflectUtil.getFields(c)).filter(it -> it.isAnnotationPresent(Ref.class)).count();
  }


  public GraphModel(int refFieldCount) {
    this._loaded = new boolean[refFieldCount];
  }

  protected final void load(int i) {
    if (_loaded[i]) return;
    doLoad(i);
  }
  protected final void doLoad(int i) {
    _loaded[i] = true;
    RefFieldInfo info = _classInfo.refFieldInfos.get(i);
    if (info.isLazy) {
      info.load(this, info.db.config.isInTransaction());
    }
  }

  /**
   使用内部db
   */
  public int update() {
    return super.update(_db);
  }


  public int save() {
    return super.save(_db);
  }

  public int delete() {
    return super.delete(_db);
  }
}