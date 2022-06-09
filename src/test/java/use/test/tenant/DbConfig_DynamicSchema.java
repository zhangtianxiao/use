package use.test.tenant;

import use.jdbc.ConnectionHolder;
import use.jdbc.DbConfig;
import use.jdbc.dialect.Dialect;
import use.kit.ObjectPool;
import use.sql.SqlManager;
import use.sql.SqlParaWriter;
import use.template.io.TextWriter;

import java.sql.Connection;
import java.sql.SQLException;

public class DbConfig_DynamicSchema extends DbConfig {
  public final ThreadLocal<String> schema_tl = new ThreadLocal<>();

  public DbConfig_DynamicSchema(String id, Dialect dialect, boolean graph, SqlManager ntm, boolean showSql, int transactionLevel
    , ConnectionHolder conn_holder
    , ObjectPool<Connection> connPool
    , ObjectPool<SqlParaWriter> dspwFactory, ObjectPool<SqlParaWriter> ispwFactory, ObjectPool<TextWriter> textWriterFactory) {
    super(id, connPool, dialect, graph, ntm, showSql, transactionLevel, conn_holder, dspwFactory, ispwFactory, textWriterFactory);
  }


  @Override
  public void setSchema(Connection connection) throws SQLException {
    // @see org.postgresql.jdbc.PgConnection.setSchema
    connection.setSchema(schema_tl.get());
  }

  public void setSchema(String name) {
    schema_tl.set(name);
  }
}
