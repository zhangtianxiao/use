package org.postgresql.jdbc;

import org.postgresql.core.Field;

import java.sql.SQLException;

public class PGHelper {
  /**
   包权限不够
   https://github.com/pgjdbc/pgjdbc/issues/2505
   */
  public static Field get(PgResultSetMetaData md, int c) throws SQLException {
    return md.getField(c);
  }
}
