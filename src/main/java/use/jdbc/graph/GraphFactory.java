package use.jdbc.graph;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ReflectUtil;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import use.beans.Beans;
import use.beans.FieldDesc;
import use.jdbc.Db;
import use.jdbc.DbKit;
import use.jdbc.RsGetter;
import use.jdbc.dialect.Dialect;
import use.kit.Func;
import use.kit.Helper;
import use.kit.ReflectKit;
import use.kit.ex.Unsupported;
import use.sql.SqlTemplate;
import use.template.Template;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static use.kit.Helper.*;


public class GraphFactory {
  private static final Logger logger = Helper.getLogger(GraphFactory.class);
  final Db db;

  public GraphFactory(Db db) {
    this.db = db;
  }

  // 有一种可能, 多数据源/分库时, 一个class被多个ref绑定
  // 为避免混淆, 把singletonCache移到Db中, build class info, field info时, 传入当前Ref上的的db
  // 也就是说classinfo 和 fieldinfo的db 一开始就能确定下来
  // 此处有api设计分歧, 入口pojo上ref的db 要不要强制填写呢,

  // 对于单纯查询场景, 全量加载, 没有什么不妥
  // 对于业务场景, lazy模式, 似乎更加合理, (layz模式和stream模式也不冲突
  // 业务场景, 查询出来当然也要做更新
  //    那么我需要知道一个field有没有被赋值过
  // 业务场景会有锁定记录的需求 不同表的多条记录, 此时就不能再用layz模式
  // 那么 layz要做成可选的, 就得有一个成员存放flags, 和 fieldinfo
  // 在业务代码层面, 成员便不能直接访问, 要通过getter, 以判断flag
  //               对值的修改 要经过setter, 以标记flag
  //  重要: 若一个field的值不是null, 那么便不该再调用load

  // BaseModel强调的是干净, 单表操作
  // GraphModel实现了lazy 多库 多表操作

  @Nullable <T> ClassInfo<T> buildClassInfo(Class<T> c, Db db, LinkedHashSet<Db> dependency) {
    dependency.add(db);
    final ThreadLocal<HashMap<Class<?>, ClassInfo>> singletonTl = db.classInfoSingletonTl;
    final ConcurrentHashMap<Class<?>, ClassInfo> singletonCache = db.classInfoSingletonCache;
    final ClassInfo<T> ci;
    HashMap<Class<?>, ClassInfo> map = singletonTl.get();
    int size = map.size();
    if (size > 0) {
      ClassInfo temp = map.get(c);
      // 发现循环注入
      if (temp != null) return temp;
    }

    synchronized (GraphFactory.class) {
      try {
        ClassInfo old = singletonCache.get(c);
        if (old == null) {
          // 构造器
          Supplier<T> maker = ReflectKit.resolveMaker(c);
          ci = new ClassInfo<>(db, maker);
          map.put(c, ci);

          //  成员信息
          List<GraphFieldInfo> fieldInfos = buildFieldInfo(ci, c, db);
          ci.setFieldInfos(fieldInfos);
          for (GraphFieldInfo fi : fieldInfos) {
            // 找具体类型的classinfo
            if (fi.isBean()) if (fi instanceof RefFieldInfo nsfi) {
              ClassInfo fieldCi = buildClassInfo(nsfi.c, nsfi.db, dependency);
              fi.classInfo = fieldCi;
            }
          }
          singletonCache.put(c, ci);
          return ci;
        }
        return old;
      } finally {
        if (size == 0) {    // 仅顶层才需要 remove()
          singletonTl.remove();
        }
      }
    }
  }

  static RefKeyInfo[] buildKeyInfos(List<GraphFieldInfo> fieldInfos, String[] keys) {
    RefKeyInfo[] refKeyInfos = new RefKeyInfo[keys.length];
    // 重要: 找到key在fields中的下标, 使用field.type创建rsGetter
    for (String key : keys) {
      boolean found = false;
      int min = Math.min(keys.length, fieldInfos.size());
      for (int i = 0; i < min; i++) {
        int c = i + 1;
        GraphFieldInfo info = fieldInfos.get(i);
        Field field = info.field;
        if (field.getName().equals(key)) {
          found = true;
          //
          refKeyInfos[i] = new RefKeyInfo(c, field, info.rsGetter, info.fieldDesc);
          break;
        }
      }
      // check:
      if (!found) throw new RuntimeException("关联字段不存在");
    }
    return refKeyInfos;
  }

  static List<GraphFieldInfo> buildFieldInfo(ClassInfo info, Class<?> clazz, Db db) {
    // pojo接口, 才扫描成员
    //if (Pojo.isPojo(c)) {
    Predicate<Field> isRefPredicate = it -> it.isAnnotationPresent(Ref.class);

    Field[] fields = ReflectUtil.getFields(clazz, NORMAL_FIELD::test);
    int[] c = {0};
    final List<GraphFieldInfo> fieldInfos = new ArrayList<>();

    for (Field it : fields) {
      Ref ref = it.getAnnotation(Ref.class);
      final Class<?> type = it.getType();
      final boolean isList = isList(type);
      final Class<?> fc;
      if (isList) {
        ParameterizedType pt = as(it.getGenericType());
        fc = as(pt.getActualTypeArguments()[0]);
        // check:
        if (isList(fc) || fc.isArray()) {
          throw new RuntimeException("不支持数组/集合嵌套");
        }
      } else fc = type;
      final boolean isPrimitive = Db.isPrimitive(fc);

      boolean isRef = isRefPredicate.test(it);
      final boolean isAny = Any.class.isAssignableFrom(type);
      Dialect dialect = db.config.dialect;

      // 缓存 rsGetter 和 FieldResolver
      final boolean isBean = !(isPrimitive || isAny);

      // 单字段取值策略 rs->value
      final RsGetter rsGetter;
      if (isBean) {
        rsGetter = null;
      } else {
        String helpMsg = clazz + "\n" + it;
        if (isRef) {
          // 单一字段取值
          rsGetter = dialect.matchRsGetter(helpMsg, fc, 1, true);
        } else {
          // 按字段顺序取值
          c[0] += 1;
          rsGetter = dialect.matchRsGetter(helpMsg, fc, c[0], false);
        }
      }

      // 赋值策略
      // bean[name] = value
      FieldDesc fieldDesc = Beans.me.desc(clazz, it.getName());
      if (fieldDesc == null)
        throw new Unsupported("未注册的赋值策略: " + clazz + "->" + it.getName());

      if (isRef) {
        boolean isGraphModel = GraphModel.class.isAssignableFrom(fc);
        boolean isLazy = ref.lazy();

        // check:
        if (isLazy && !isGraphModel) throw new Unsupported("lazy=true 只能标记在GraphModel的子类上: " + fc);

        String[] keys = ref.keys();
        RefKeyInfo[] refKeyInfos = buildKeyInfos(fieldInfos, keys);
        final String fieldSQlKey = ref.value();
        String breakCondExpr = ref.breakCond();
        final Template breakCond = breakCondExpr.isEmpty() ? null : Helper.engine.getTemplateByString("#(" + breakCondExpr + ")");
        String dbName = ref.db();

        final Db currentDb = dbName.isEmpty() ? db : DbKit.db(dbName);
        final SqlTemplate fieldDbTemplate = currentDb.template(fieldSQlKey);

        GraphFieldInfo ret = new RefFieldInfo(currentDb, isGraphModel, isLazy, isList, isPrimitive, false, it, rsGetter, fieldDesc, fieldDbTemplate, fc, refKeyInfos, breakCond);
        fieldInfos.add(ret);
      } else {
        final PrimitiveFieldInfo ret = new PrimitiveFieldInfo(isList, isPrimitive, isAny, it, rsGetter, fieldDesc);
        fieldInfos.add(ret);
      }
    }
    return fieldInfos;
  }


  /**
   仅在调用入口处hold conn,
   1. 大多数场景都是单一数据库
   2.
   - 在graph.tx()中, 会自动hold住所有依赖的conn
   -  而普通的graph调用 配合lazy, 一直hold conn, 会降低连接利用率
   */
  private static <R, A> R holdConnection(Func.Func3<Db, Map, A, R> fn, Db db, @NotNull Map map, A a) {
    Connection conn = db.getConn();
    boolean tx_running = db.config.isInTransaction();
    // 如果原来没有开事务, 这里也不开事务,
    // 但是需要复用连接
    if (tx_running) {
    } else db.config.conn_holder.set(conn);
    R ret = null;
    try {
      ret = fn.apply(db, map, a);
    } finally {
      // 如果开了事务, 则不处理
      if (tx_running) {
      }
      //
      else {
        db.config.conn_holder.remove();
        db.config.tryClose(conn);
      }
    }
    return ret;
  }

  public <T> Graph<T> one(Class<T> c) {
    Ref ref = c.getAnnotation(Ref.class);
    String SQLKey = ref.value();
    SqlTemplate dbTemplate = this.db.template(SQLKey);
    // fields[0].getType().newInstance();
    LinkedHashSet<Db> dependency = new LinkedHashSet<>();
    ClassInfo<T> ci = buildClassInfo(c, this.db, dependency);

    Func.Func3<Db, Map, Void, T> beanFn = (db, map, avoid) -> {
      T select = ci.one(dbTemplate, map, null, 0);
      return select;
    };

    Func.Func3<Db, Map, OutputStream, List<T>> streamFn = (db, map, out) -> {
      JsonStream stream = new JsonStream(out, 2048);
      ci.one(dbTemplate, map, stream, 0);
      IoUtil.flush(stream);
      return null;
    };

    return new Graph<>(db, dependency) {
      @Override
      public T get(Map map) {
        T t = holdConnection(beanFn, db, map, null);
        return t;
      }

      @Override
      public void streamAsJson(Map map, OutputStream out) {
        holdConnection(streamFn, db, map, out);
      }
    };
  }


  public <T> Graph<List<T>> list(Class<T> c) {
    Ref ref = c.getAnnotation(Ref.class);
    String SQLKey = ref.value();
    SqlTemplate dbTemplate = db.template(SQLKey);
    // fields[0].getType().newInstance();
    LinkedHashSet<Db> dependency = new LinkedHashSet<>();
    ClassInfo<T> ci = buildClassInfo(c, this.db, dependency);

    Func.Func3<Db, Map, Void, List<T>> beanFn = (db, map, avoid) -> {
      List<T> list = ci.list(dbTemplate, map, null, 0);
      return list;
    };

    Func.Func3<Db, Map, OutputStream, List<T>> streamFn = (db, map, out) -> {
      JsonStream stream = new JsonStream(out, 2048);
      List<T> list = ci.list(dbTemplate, map, stream, 0);
      IoUtil.flush(stream);
      return list;
    };

    return new Graph(db, dependency) {
      @Override
      public List<T> get(Map map) {
        return holdConnection(beanFn, db, map, null);
      }

      @Override
      public void streamAsJson(Map map, OutputStream out) {
        holdConnection(streamFn, db, map, out);
      }
    };
  }

  static boolean checkDepth(int n) {
    boolean b = n > 100;
    if (b) logger.warn("db graph: 嵌套层级过深, 可能出现死循环, 请检查数据, 及时跳出");
    return b;
  }

}