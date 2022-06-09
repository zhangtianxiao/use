package use.jdbc.dialect;

import com.fasterxml.jackson.databind.JsonNode;
import com.jsoniter.any.Any;
import org.postgresql.core.Field;
import org.postgresql.core.Oid;
import org.postgresql.jdbc.PgResultSetMetaData;
import org.postgresql.util.PGobject;
import use.jdbc.ActiveRecordException;
import use.sql.SqlParaWriter;
import use.kit.ex.Unsupported;
import use.template.io.TextWriter;
import use.kit.ReflectKit;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static use.kit.Helper.*;

/**
 对类型的判断目前基于字符串, pg驱动中似乎没返回pg type @see org.postgresql.core.Field#NOT_YET_LOADED
 sqlType不足以区分java.sql.Array 和 PgObject的类型 */
public class PostgresqlDialect extends Dialect {
  public PostgresqlDialect(boolean ignore_schema){
    super(ignore_schema);
  }
  @Override
  public void forTableBuilderDoBuild(String schema, String table, SqlParaWriter writer) {
    //writer.reset();
    writer.writeVal("select * from \"");
    if (!ignore_schema) {
      writer.writeVal(schema);
      writer.writeVal("\".\"");
    }
    writer.writeVal(table);
    writer.writeVal("\" where 1 = 2");
  }

  @Override
  public void forDbFindById(String schema, String table, Long id, SqlParaWriter writer) {
    //writer.reset();
    writer.writeVal("select * from \"");
    if (!ignore_schema) {
      writer.writeVal(schema);
      writer.writeVal("\".\"");
    }
    writer.writeVal(table);
    writer.writeVal("\" where id = ?");
    writer.paras.add(id);
  }

  @Override
  public void forDbDeleteById(String schema, String table, Long id, SqlParaWriter writer) {
    //writer.reset();
    writer.writeVal("delete from \"");
    if (!ignore_schema) {
      writer.writeVal(schema);
      writer.writeVal("\".\"");
    }
    writer.writeVal(table);
    writer.writeVal("\" where id = ?");
    writer.paras.add(id);
  }

  @Override
  public void forDbSave(String schema, String table, Map<String, Object> data, SqlParaWriter writer, TextWriter temp) {
    List<Object> paras = writer.paras;
    writer.writeVal("insert into \"");
    if (!ignore_schema) {
      writer.writeVal(schema);
      writer.writeVal("\".\"");
    }
    writer.writeVal(table);
    if (data.size() == 0) {
      writer.writeVal("\" default values");
    }
    else {
      writer.writeVal("\"(");
      temp.writeVal(") values(");
      for (Map.Entry<String, Object> e : data.entrySet()) {
        String colName = e.getKey();
        if (paras.size() > 0) {
          writer.writeVal(", ");
          temp.writeVal(", ");
        }
        writer.writeVal("\"");
        writer.writeVal(colName);
        writer.writeVal("\"");
        temp.writeVal('?');
        paras.add(e.getValue());
      }
      temp.writeTo((TextWriter) writer.delegate);
      writer.writeVal(")");
    }
  }

  @Override
  public void forDbUpdate(String schema, String table, Map<String, Object> data, Long id, SqlParaWriter writer) {
    //writer.reset();
    List<Object> paras = writer.paras;
    paras.clear();
    writer.writeVal("update \"");
    if (!ignore_schema) {
      writer.writeVal(schema);
      writer.writeVal("\".\"");
    }
    writer.writeVal(table);
    writer.writeVal("\" set ");
    boolean first = true;
    Set<Map.Entry<String, Object>> entries = data.entrySet();
    for (Map.Entry<String, Object> entry : entries) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (first) {
        first = false;
        writer.writeVal("\"");
      }
      else {
        writer.writeVal(",\"");
      }

      writer.writeVal(key);
      writer.writeVal("\"");
      writer.writeVal("=?");
      paras.add(value);
    }
    writer.writeVal(" where id = ?");
    paras.add(id);
  }

  /**
   PGobject jsonb能用byte[]传递最好了, 少去to string copy
   */
  @Override
  public Object convertForSend(Object v) {
    if (v instanceof JsonNode) {
      PGobject pGobject = new PGobject();
      pGobject.setType("jsonb");
      try {
        pGobject.setValue(v.toString());
      } catch (SQLException e) {
        throw ActiveRecordException.wrap(e);
      }
      return pGobject;
    }
    else if (v instanceof Any) {
      PGobject pGobject = new PGobject();
      pGobject.setType("jsonb");
      try {
        pGobject.setValue(v.toString());
      } catch (SQLException e) {
        throw ActiveRecordException.wrap(e);
      }
      return pGobject;
    }
    return v;
  }

  protected static java.lang.reflect.Field FieldsField = ReflectKit.getField(PgResultSetMetaData.class, "fields");

  public static int oid(PgResultSetMetaData md, int c) throws SQLException {
    // 如果pg驱动这里放开, 就不用反射了
    Field[] fields = as(ReflectKit.getFieldValue(md, FieldsField));
    Field field = fields[c - 1];
    return field.getOID();
  }


  /**
   *
   */
  @Override
  public Object convertForRead(ResultSet rs, int c) throws SQLException {
    PgResultSetMetaData md = as(rs.getMetaData());
    int ct = oid(md, c);
    //int ct = md.getColumnType(c);
    if (ct == Oid.TEXT || ct == Oid.VARCHAR)
      return rs.getString(c);

    if (ct == Oid.INT4)
      return rs.getObject(c);
    if (ct == Oid.INT8)
      return rs.getObject(c);

    if (ct == Oid.FLOAT4)
      return rs.getObject(c);
    if (ct == Oid.FLOAT8)
      return rs.getObject(c);
    if (ct == Oid.NUMERIC)
      return rs.getObject(c);
    if (ct == Oid.BOOL)
      return rs.getObject(c);

    if (ct == Oid.DATE)
      return rs.getObject(c, java.time.LocalDate.class);
    if (ct == Oid.TIMESTAMP)
      return rs.getObject(c, java.time.LocalDateTime.class);
    /*
    驱动中始终保持utc时间
    到了用户手中又要转一次 withOffsetSameInstant, 有点浪费
    其实用字符串可能更省心
    org/postgresql/jdbc/TimestampUtils.java:503
    @see Postgres is always UTC
    * */
    if (ct == Oid.TIMESTAMPTZ) {
      OffsetDateTime object = rs.getObject(c, OffsetDateTime.class);
      //return object;
      return object.withOffsetSameInstant(zoneOffset);
    }

    if (ct == Oid.TIME)
      return rs.getObject(c, java.time.LocalTime.class);
    if (ct == Oid.TIMETZ) {
      OffsetTime object = rs.getObject(c, OffsetTime.class);
      //return object;
      return object.withOffsetSameInstant(zoneOffset);
    }

    if (ct == Oid.INT4_ARRAY || ct == Oid.INT8_ARRAY || ct == Oid.TEXT_ARRAY || ct == Oid.VARCHAR_ARRAY)
      return as(rs.getArray(c).getArray());

    if (ct == Oid.JSON || ct == Oid.JSONB) {
      /*PGobject pGobject = as(rs.getObject(c));
      String value = pGobject.getValue();
      if(value==null) return  null;
      return JsonIterator.deserialize(value)*/
      byte[] bytes = rs.getBytes(c);
      if (bytes == null || bytes.length == 0)
        return null;
      return createJsonNode(bytes);
    }
    String name = md.getColumnName(c);
    throw new Unsupported("name: " + name + " ct: " + ct);
  }

  /**
   目前只是postgres实现
   社区分享：《Oracle NUMBER 类型映射改进》https://jfinal.com/share/1145
   */
  public String handleJavaType(ResultSetMetaData rsmd, int c) throws SQLException {
    int ct = oid(as(rsmd), c);

    if (ct == Oid.INT4)
      return "java.lang.Integer";
    if (ct == Oid.INT8)
      return "java.lang.Long";
    if (ct == Oid.FLOAT4)
      return "java.lang.Float";
    if (ct == Oid.FLOAT8)
      return "java.lang.Double";
    if (ct == Oid.NUMERIC)
      return "java.math.BigDecimal";
    if (ct == Oid.BOOL)
      return "java.lang.Boolean";

    if (ct == Oid.DATE)
      return "java.time.LocalDate";
    if (ct == Oid.TIMESTAMP)
      return "java.time.LocalDateTime";
    if (ct == Oid.TIMESTAMPTZ)
      return "java.time.OffsetDateTime";

    if (ct == Oid.TIME)
      return "java.time.LocalTime";
    if (ct == Oid.TIMETZ)
      return "java.time.OffsetTime";

    if (ct == Oid.VARCHAR || ct == Oid.TEXT)
      return "java.lang.String";

    if (ct == Oid.INT4_ARRAY)
      return "java.lang.Integer[]";

    if (ct == Oid.INT8_ARRAY)
      return "java.lang.Long[]";

    if (ct == Oid.TEXT_ARRAY || ct == Oid.VARCHAR_ARRAY)
      return "java.lang.String[]";
    if (ct == Oid.JSON || ct == Oid.JSONB)
      return "com.fasterxml.jackson.databind.JsonNode";
    String name = rsmd.getColumnName(c);
    throw new Unsupported(" ct: " + ct + " columnName: " + name);
  }

}
