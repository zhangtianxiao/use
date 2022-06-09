package use.jdbc;

import use.jdbc.dialect.Dialect;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class RsHandler<T> {
  /**
   atta 附加对象
   */
  public abstract T run(ResultSet rs, Dialect dialect, Object atta, int depth) throws SQLException;

  public T run(ResultSet rs, Dialect dialect, Object atta) throws SQLException {
    return this.run(rs, dialect, atta, 0);
  }
}
