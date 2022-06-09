

package use.jdbc;

import org.jetbrains.annotations.NotNull;
import use.jdbc.dialect.Dialect;
import use.kit.ObjectPool;
import use.sql.SqlManager;
import use.sql.SqlParaWriter;
import use.template.io.TextWriter;

import java.sql.Connection;
import java.sql.SQLException;

/**
 jdbc的DataSource接口没有recycle方法
 故主流ds库都会做一层wrap, 重写connection.close做回收而非断开的逻辑
 用户调用conn.close就算回收
 */
public class DbConfig {
  public final ConnectionHolder conn_holder;

  public final String id;
  public final ObjectPool<Connection> connPool;
  public final boolean graph;

  public final SqlManager sm;
  public final Dialect dialect;
  public final boolean showSql;
  public final int transactionLevel;

  public final ObjectPool<SqlParaWriter> dspwFactory;
  public final ObjectPool<SqlParaWriter> ispwFactory;
  public final ObjectPool<TextWriter> textWriterFactory;

  public DbConfig(String id, ObjectPool<Connection> connPool, Dialect dialect, boolean graph
    , SqlManager sm, boolean showSql, int transactionLevel
    , ConnectionHolder conn_holder
    , ObjectPool<SqlParaWriter> dspwFactory
    , ObjectPool<SqlParaWriter> ispwFactory
    , ObjectPool<TextWriter> textWriterFactory
  ) {
    this.id = id.trim();
    this.connPool = connPool;
    this.dialect = dialect;
    this.graph = graph;
    this.sm = sm;
    this.showSql = showSql;
    // this.transactionLevel = transactionLevel;
    int t = transactionLevel;
    if (t != 0 && t != 1 && t != 2 && t != 4 && t != 8) {
      throw new IllegalArgumentException("The transactionLevel only be 0, 1, 2, 4, 8");
    }
    this.transactionLevel = transactionLevel;
    this.conn_holder = conn_holder;
    this.dspwFactory = dspwFactory;
    this.ispwFactory = ispwFactory;
    this.textWriterFactory = textWriterFactory;
  }

  public boolean isShowSql() {
    return showSql;
  }


  @NotNull
  public Connection getConnection() {
    return getConnection(transactionLevel, true);
  }

  @NotNull
  public Connection getConnection(boolean autoCommit) {
    return getConnection(transactionLevel, autoCommit);
  }

  @NotNull
  public Connection getConnection(int level, boolean autoCommit) {
    //System.out.println("#getConnection: " + id);
    Connection conn = conn_holder.get();
    if (conn != null)
      return conn;
    //return showSql ? new SqlReporter(dataSource.getConnection()).getConnection() : dataSource.getConnection();
    try {
      conn = connPool.get();
      conn.setAutoCommit(autoCommit);
      conn.setTransactionIsolation(level);
      setSchema(conn);
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
    return showSql ? new SqlReporter(id, conn).getConnection() : conn;
  }

  public void setSchema(Connection connection) throws SQLException {
  }

  public Connection begin() {
    return begin(transactionLevel);
  }

  public Connection begin(int level) {
    Connection conn = conn_holder.get();
    if (conn != null)
      return conn;
    Connection connection = getConnection(level, false);
    conn_holder.set(connection);
    return connection;
  }


  /**
   Return true if current thread in transaction.
   */
  public boolean isInTransaction() {
    return conn_holder.get() != null;
  }


  public void tryClose(Connection conn) {
    if (conn_holder.get() == null)    // in transaction if conn in threadlocal
      connPool.recycle(conn);
  }


  public int getTransactionLevel() {
    return transactionLevel;
  }

  public void commit() {
    Connection conn = conn_holder.get();
    if (conn != null)
      commit(conn);
  }


  public void commit(Connection conn) {
    conn_holder.set(null);
    try {
      //if (!connection.getAutoCommit())
      conn.commit();
    } catch (SQLException e) {
      // 保留commit 失败的可能性
      throw ActiveRecordException.wrap(e);
    } finally {
      connPool.recycle(conn);
    }
  }


  public void rollback() {
    Connection conn = conn_holder.get();
    if (conn != null)
      rollback(conn);
  }


  public void rollback(Connection conn) {
    conn_holder.set(null);
    try {
      //if (!connection.getAutoCommit())
      conn.rollback();
    } catch (SQLException e) {
      // 保留rollback 失败的可能性
      throw ActiveRecordException.wrap(e);
    } finally {
      connPool.recycle(conn);
    }
  }

}



