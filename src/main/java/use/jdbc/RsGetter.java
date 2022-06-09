package use.jdbc;

import use.jdbc.dialect.Dialect;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 单字段取值
 性能敏感,不使用lambda
 ! */
public abstract class RsGetter extends RsHandler<Object> {
  public final int c;
  public final Class type;
  public final Dialect dialect;
  public final boolean callNext;
  public final String helpMsg;

  public RsGetter(String helpMsg,int c, Class type, Dialect dialect, boolean callNext) {
    this.helpMsg = helpMsg;
    this.c = c;
    this.type = type;
    this.dialect = dialect;
    this.callNext = callNext;
  }

  public Object run(ResultSet rs) throws SQLException {
    return run(rs, this.dialect, null);
  }

  @Override
  public Object run(ResultSet rs, Dialect dialect, Object atta, int depth) throws SQLException {
    boolean next = true;
    if (callNext)
      next = rs.next();
    if (next)
      return convert(rs.getObject(c, type));
    else
      return null;
  }

  public Object convert(Object o) {
    return o;
  }

}
