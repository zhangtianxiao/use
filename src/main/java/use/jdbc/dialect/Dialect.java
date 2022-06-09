

package use.jdbc.dialect;

import com.fasterxml.jackson.databind.JsonNode;
import com.jsoniter.any.Any;
import use.jdbc.Db;
import use.jdbc.RsGetter;
import use.sql.SqlParaWriter;
import use.kit.ex.Unsupported;
import use.template.io.TextWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 Dialect. */
public abstract class Dialect {
  public final boolean ignore_schema;

  public Dialect(boolean ignore_schema) {
    this.ignore_schema = ignore_schema;
  }

  public Dialect() {
    ignore_schema = false;
  }
  // Methods for common
  public abstract void forTableBuilderDoBuild(String schema, String table, SqlParaWriter writer);

  // Methods for Model

  // Methods for DbPro. Do not delete the String[] pKeys parameter, the element of pKeys needs to trim()
  public abstract void forDbFindById(String schema, String table, Long id, SqlParaWriter writer);

  public abstract void forDbDeleteById(String schema, String table, Long id, SqlParaWriter writer);

  public abstract void forDbSave(String schema, String table, Map<String, Object> data, SqlParaWriter writer, TextWriter temp);

  public abstract void forDbUpdate(String schema, String table, Map<String, Object> data, Long id, SqlParaWriter writer);

  public void forFindAll(String schema, String table, SqlParaWriter writer) {
    writer.writeVal("select * from \"");
    if(!ignore_schema){
      writer.writeVal(schema);
      writer.writeVal("\".\"");
    }
    writer.writeVal(table);
    writer.writeVal("\"");
  }

  public boolean isOracle() {
    return false;
  }


  /**
   生成的model, 类型已知, 如json类型会映射到JsonNode 不需要此方法
   而graph实体来说, 为了更好的性能, json字段会映射到Any,
   实体类字段 -> rs.get
   */
  public RsGetter matchRsGetter(String helpMsg, Class<?> type, int c, boolean callNext) {
    final RsGetter rsGetter;
    boolean primitive = Db.isPrimitive(type);
    if (primitive) {
      if (OffsetTime.class.isAssignableFrom(type)) {
        rsGetter = new OffsetTimeGetter(helpMsg, c, type, this, callNext);
      }
      else if (OffsetDateTime.class.isAssignableFrom(type)) {
        rsGetter = new OffsetDateTimeGetter(helpMsg, c, type, this, callNext);
      }
      else if (JsonNode.class.isAssignableFrom(type)) {
        rsGetter = new JsonNodeGetter(helpMsg, c, type, this, callNext);
      }
      else {
        rsGetter = new PrimitiveGetter(helpMsg, c, type, this, callNext);
      }
    }
    else if (Any.class.isAssignableFrom(type)) {
      rsGetter = new AnyGetter(helpMsg, c, type, this, callNext);
    }
    else {
      throw new Unsupported("未适配的类型: " + type);
    }
    return rsGetter;
  }

  public Object convertForSend(Object v) {
    return v;
  }

  public Object convertForRead(ResultSet rs, int c) throws SQLException {
    return rs.getObject(c);
  }


  public void fillStatement(PreparedStatement pst, List<Object> paras) throws SQLException {
    for (int i = 0, size = paras.size(); i < size; i++) {
      pst.setObject(i + 1, convertForSend(paras.get(i)));
    }
  }

/*  public void fillStatement(PreparedStatement pst, Object... paras) throws SQLException {
    for (int i = 0; i < paras.length; i++) {
      pst.setObject(i + 1, convertForSend(paras[i]));
    }
  }*/

  public void fillStatement(PreparedStatement pst, Iterator<Object> paras) throws SQLException {
    int i = 0;
    while (paras.hasNext()) {
      i += 1;
      pst.setObject(i, convertForSend(paras.next()));
    }
  }

  public void fillStatement(PreparedStatement pst, Object[] paras) throws SQLException {
    int i = 0;
    for (Object para : paras) {
      i += 1;
      pst.setObject(i, convertForSend(para));
    }

  }

  public String getDefaultPrimaryKey() {
    return "id";
  }

  public boolean isPrimaryKey(String colName, String[] pKeys) {
    return false;
  }

  /**
   目前只是postgres实现
   社区分享：《Oracle NUMBER 类型映射改进》https://jfinal.com/share/1145
   */
  public abstract String handleJavaType(ResultSetMetaData rsmd, int c) throws SQLException;

}






