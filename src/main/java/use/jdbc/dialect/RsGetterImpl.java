package use.jdbc.dialect;


import com.jsoniter.JsonIterator;
import use.jdbc.ActiveRecordException;
import use.jdbc.RsGetter;
import use.kit.Helper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

import static use.kit.Helper.as;

/**
 基本类型 包装类型 localdatetime不带offset */
class PrimitiveGetter extends RsGetter {

  public PrimitiveGetter(String helpMsg,int c, Class type, Dialect dialect, boolean callNext) {
    super(helpMsg,c, type, dialect, callNext);
  }

  @Override
  public Object run(ResultSet rs, Dialect dialect, Object atta, int depth) throws SQLException {
    boolean next = true;
    if (callNext)
      next = rs.next();
    if (next)
      return rs.getObject(c, type);
    else
      return null;
  }
}

class OffsetTimeGetter extends RsGetter {

  public OffsetTimeGetter(String helpMsg,int c, Class type, Dialect dialect, boolean callNext) {
    super(helpMsg,c, type, dialect, callNext);
  }

  @Override
  public Object convert(Object o) {
    OffsetTime value = as(o);
    return value.withOffsetSameInstant(Helper.zoneOffset);
  }
}

class OffsetDateTimeGetter extends RsGetter {

  public OffsetDateTimeGetter(String helpMsg,int c, Class type, Dialect dialect, boolean callNext) {
    super(helpMsg,c, type, dialect, callNext);
  }

  @Override
  public Object convert(Object o) {
    OffsetDateTime value = as(o);
    return value.withOffsetSameInstant(Helper.zoneOffset);
  }
}


class AnyGetter extends RsGetter {

  public AnyGetter(String helpMsg,int c, Class type, Dialect dialect, boolean callNext) {
    super(helpMsg,c, type, dialect, callNext);
  }

  @Override
  public Object run(ResultSet rs, Dialect dialect, Object atta, int depth) throws SQLException {
    //byte[] value = rs.getObject(c, byte[].class);
    boolean next = true;
    if (callNext)
      next = rs.next();
    if (next)
      try {
        byte[] value = rs.getBytes(c);
        return JsonIterator.deserialize(value);
      } catch (Throwable e) {
        throw new ActiveRecordException(e);
      }
    else
      return null;


  }

}

class JsonNodeGetter extends RsGetter {

  public JsonNodeGetter(String helpMsg,int c, Class type, Dialect dialect, boolean callNext) {
    super(helpMsg,c, type, dialect, callNext);
  }

  @Override
  public Object run(ResultSet rs, Dialect dialect, Object atta, int depth) throws SQLException {
    //byte[] value = rs.getObject(c, byte[].class);
    boolean next = true;
    if (callNext)
      next = rs.next();
    if (next)
      try {
        byte[] value = rs.getBytes(c);
        return Helper.createJsonNode(value);
      } catch (Throwable e) {
        throw new ActiveRecordException(e);
      }
    else
      return null;
  }

}