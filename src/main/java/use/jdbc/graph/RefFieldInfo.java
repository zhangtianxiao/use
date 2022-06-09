package use.jdbc.graph;

import use.beans.FieldDesc;
import use.jdbc.Db;
import use.jdbc.RsGetter;
import use.sql.SqlTemplate;
import use.template.Template;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import static use.kit.Helper.as;

/**
 stream模式没有必要走getter
 !
 */
public class RefFieldInfo extends GraphFieldInfo {

  public RefFieldInfo(
    Db db,
    boolean isGraphModel, boolean isLazy
    , boolean isList, boolean isPrimitive, boolean isAny, Field field
    , RsGetter rsGetter, FieldDesc fieldResolver,
    SqlTemplate sqlTemplate, Class c, RefKeyInfo[] refKeyInfos,
    Template breakCond
  ) {
    super(isList, isPrimitive, isAny, field, rsGetter, fieldResolver);
    this.db = db;
    this.isGraphModel = isGraphModel;
    this.isLazy = isLazy;
    this.sqlTemplate = sqlTemplate;
    this.c = c;
    this.refKeyInfos = refKeyInfos;
    this.breakCond = breakCond;
  }

  public final Db db;
  public final boolean isLazy;
  public final boolean isGraphModel;
  final SqlTemplate sqlTemplate;
  final Class c;
  final RefKeyInfo[] refKeyInfos;
  final Template breakCond;

  @Override
  boolean isNs() {
    return true;
  }


  public void load(Object it, boolean begin) {
    this.load(it, new HashMap(), 1, begin);
  }

  public void load(Object it, HashMap condition, int depth, boolean beginning) {
    if (GraphFactory.checkDepth(depth))
      return;
    RefFieldInfo info = this;
    // 关联字段 判断是否skip
    if (info.refKeyInfos.length != 0) {
      //condition = new HashMap();
      for (RefKeyInfo refKeyInfo : info.refKeyInfos) {
        //int c = refKeyInfo.column;
        String cName = refKeyInfo.name;
        //final Object cValue = refKeyInfo.rsGetter.run(rs);
        Object cValue = db.config.sm.engine.config.beans.get(it, cName);
        condition.put(cName, cValue);
      }
    }
    final boolean skip;
    if (info.breakCond != null) {
      Object eval = info.breakCond.eval(condition);
      skip = eval == null || (Boolean) eval;
    } else
      skip = false;

    if (!skip) {
      final Object value = info.doLoad(condition, depth, beginning);
      String name = info.field.getName();
      info.fieldDesc.setter.set(it, value);
    }
  }

  Object doLoad(HashMap<String, Object> condition, int depth, boolean begin) {
    RefFieldInfo info = this;
    SqlTemplate sqlTemplate = info.sqlTemplate;
    // 重要: 混合事务
    if (begin)
      db.config.begin();


    final Object value;
    // 递归 list
    if (info.isList) {
      //value = dbTemplate.select(condition, info.classInfo.toList, null);
      value = info.classInfo.list(sqlTemplate, condition, null, depth);
      if (info.isGraphModel) {
        List list = as(value);
        for (Object o : list)
          fillModelInfo(o);
      }
    }
    // 常规 有一点点弱类型
    else if (info.isPrimitive) {
      value = db.select(sqlTemplate, condition, info.rsGetter, null);
    }
    // 递归 bean
    else {
      value = info.classInfo.one(sqlTemplate, condition, null, depth);
      //value = dbTemplate.select(condition, info.classInfo.toOne, null);
      if (info.isGraphModel) {
        fillModelInfo(value);
      }
    }
    return value;
  }

  private void fillModelInfo(Object t) {
    RefFieldInfo info = this;
    // graphModel 内置 classinfo db
    if (info.isGraphModel) {
      GraphModel gm = as(t);
      gm._classInfo = info.classInfo;
      gm._db = info.db;
    }
  }

}