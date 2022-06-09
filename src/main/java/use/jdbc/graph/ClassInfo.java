package use.jdbc.graph;

import com.jsoniter.output.JsonStream;
import org.slf4j.Logger;
import use.jdbc.ActiveRecordException;
import use.jdbc.Db;
import use.jdbc.RsHandler;
import use.jdbc.dialect.Dialect;
import use.sql.SqlTemplate;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static use.kit.Helper.getLogger;

/**
 实体类才会生成 ClassInfo

 classinfo, fieldinfo

 !
 */
public class ClassInfo<T> {
  private static final Logger logger = getLogger(ClassInfo.class);
  final Db db;
  final Supplier<T> maker;

  public ClassInfo(Db db, Supplier<T> maker) {
    this.db = db;
    this.maker = maker;
  }

  // 递归时, 用map做延迟标记, 成员就不能final
  public List<GraphFieldInfo> fieldInfos;
  public List<RefFieldInfo> refFieldInfos;
  public List<GraphFieldInfo> primitiveFieldInfos;

  public void setFieldInfos(List<GraphFieldInfo> fieldInfos) {
    this.fieldInfos = fieldInfos;
    Predicate<GraphFieldInfo> isNs = GraphFieldInfo::isNs;
    this.refFieldInfos = fieldInfos.stream().filter(isNs).map(it -> (RefFieldInfo) it).toList();
    this.primitiveFieldInfos = fieldInfos.stream().filter(isNs.negate()).toList();
  }

  /**
   rs -> bean
   */
  private T makeOne(ResultSet rs, final HashMap<String, Object> condition, int depth) throws SQLException {
    // 1. 构造器
    T t = maker.get();

    // 2. 常规字段, 按字段顺序读取rs, 而不是name, 效率更高
    for (int i = 0; i < primitiveFieldInfos.size(); i++) {
      GraphFieldInfo info = primitiveFieldInfos.get(i);
      Field field = info.field;
      String name = field.getName();
      final Object value;
      try {
        value = info.rsGetter.run(rs);
      } catch (Throwable e) {
        logger.error(info.rsGetter.helpMsg);
        throw e;
      }
      info.fieldDesc.setter.set(t, value);
    }

    // 2. 引用字段
    final int nextDepth = depth + 1;
    if (GraphFactory.checkDepth(nextDepth))
      return t;
    // 已在事务中
    boolean inTx = db.config.isInTransaction();
    for (int i = 0; i < refFieldInfos.size(); i++) {
      RefFieldInfo info = refFieldInfos.get(i);
      // 主要: 混合事务
      if (inTx && info.db != db) {
        // 如果之前开了事务, 那么要跟着开事务
        // 开了第二个事务, 却无法预测它何时结束, 便只能在整个任务结束后, 统一回收连接
        info.db.config.begin();
      }
      if (!info.isLazy) {
        // 图省事, 第一行直接clear
        condition.clear();
        // 解析下一个bean字段
        info.load(t, condition, nextDepth, inTx);
      }
    }
    return t;
  }

  /**
   rs -> stream
   stream是只读的, 暂时不扩展混合事务的判断
   */
  private void makeOne(ResultSet rs, JsonStream stream, final HashMap<String, Object> condition, int depth) throws SQLException, IOException {
    boolean first = true;
    stream.writeObjectStart();
    // 1. 常规字段
    for (int i = 0; i < primitiveFieldInfos.size(); i++) {
      if (first) first = false;
      else stream.writeMore();

      GraphFieldInfo info = primitiveFieldInfos.get(i);
      String name = info.field.getName();
      Object value = info.rsGetter.run(rs);
      stream.writeObjectField(name);
      stream.writeVal(value);
    }


    // 2. 引用字段
    final int nextDepth = depth + 1;
    if (GraphFactory.checkDepth(nextDepth))
      return;
    for (int i = 0; i < refFieldInfos.size(); i++) {
      // 图省事, 第一行直接clear
      condition.clear();
      RefFieldInfo info = refFieldInfos.get(i);


      // 关联字段
      if (info.refKeyInfos.length != 0) {
        for (RefKeyInfo refKeyInfo : info.refKeyInfos) {
          //Object value = Global.resolve(one, key);
          int c = refKeyInfo.column;
          String cName = refKeyInfo.name;
          final Object cValue = refKeyInfo.rsGetter.run(rs);
          condition.put(cName, cValue);
        }
      }
      if (info.breakCond != null) {
        Object eval = info.breakCond.eval(condition);
        if (eval == null || (Boolean) eval) {

          // 2.1 跳过, 写null
          if (first) first = false;
          else stream.writeMore();
          String name = info.field.getName();
          stream.writeObjectField(name);
          stream.writeNull();
          continue;
        }
      }

      // 2.2 递归输出引用字段
      if (first) first = false;
      else stream.writeMore();

      // 已在事务中
      boolean inTx = this.db.config.isInTransaction();
      SqlTemplate sqlTemplate = info.sqlTemplate;
      // 主要: 混合事务
      if (inTx && info.db != db) {
        // 如果之前开了事务, 那么要跟着开事务
        // 开了第二个事务, 却无法预测它何时结束, 便只能在整个任务结束后, 统一回收连接
        info.db.config.begin();
      }

      // 出于性能考虑暂时注掉
      // 无关联字段使用one -> it
      //else condition.put("it", one);
      String name = info.field.getName();
      stream.writeObjectField(name);
      if (info.isList) {
        // 递归
        info.classInfo.list(sqlTemplate, condition, stream, nextDepth);
        //dbTemplate.select(condition, info.classInfo.toList, stream);
      } else if (info.isPrimitive) {
        // 有一点点弱类型
        Object primitive = info.db.getField(sqlTemplate,condition, info.rsGetter);
        stream.writeVal(primitive);
      }
      // bean
      else {
        // 递归
        info.classInfo.one(sqlTemplate, condition, stream, nextDepth);
        //dbTemplate.select(condition, info.classInfo.toOne, stream);
      }
    }
    stream.writeObjectEnd();
  }

  /**
   makeOne是解析bean
   */
  private final RsHandler<List<T>> toList = new RsHandler<>() {
    @Override
    public List<T> run(ResultSet rs, Dialect dialect, Object atta, int depth) throws SQLException {
      if (atta == null) {
        ArrayList<T> list = new ArrayList<>();
        HashMap<String, Object> temp = new HashMap<>();
        while (rs.next()) {
          T t = makeOne(rs, temp, depth);
          list.add(t);
        }
        return list;
      } else if (atta instanceof JsonStream stream) {
        try {
          stream.writeArrayStart();
          boolean first = true;
          HashMap<String, Object> temp = new HashMap<>();
          while (rs.next()) {
            if (first) first = false;
            else stream.writeMore();
            makeOne(rs, stream, temp, depth);
          }
          stream.writeArrayEnd();

        } catch (IOException e) {
          throw ActiveRecordException.wrapEx(e);
        }
      }
      return null;
    }
  };

  private final RsHandler<T> toOne = new RsHandler<>() {
    @Override
    public T run(ResultSet rs, Dialect dialect, Object atta, int depth) throws SQLException {
      HashMap<String, Object> temp = new HashMap<>();
      if (atta == null) {
        if (rs.next())
          return makeOne(rs, temp, depth);
        return null;
      } else if (atta instanceof JsonStream stream) {
        try {
          if (rs.next())
            makeOne(rs, stream, temp, depth);
          else
            stream.writeNull();

        } catch (IOException e) {
          throw ActiveRecordException.wrapEx(e);
        }
      }
      return null;
    }
  };

  /**
   classinfo 是查询的入口, toList是嵌套执行
   */
  public List<T> list(SqlTemplate sqlTemplate, Map map, Object atta, int depth) {
    if (GraphFactory.checkDepth(depth))
      return null;
    return db.select(sqlTemplate, map, this.toList, atta);
  }

  public T one(SqlTemplate sqlTemplate, Map map, Object atta, int depth) {
    if (GraphFactory.checkDepth(depth))
      return null;
    return db.select(sqlTemplate, map, this.toOne, atta);
  }


}