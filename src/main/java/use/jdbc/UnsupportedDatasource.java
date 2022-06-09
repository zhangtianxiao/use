package use.jdbc;

import use.kit.ex.Unsupported;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class UnsupportedDatasource implements DataSource {
  public final DataSource ds;

  public UnsupportedDatasource(DataSource ds) {
    this.ds = ds;
  }


  AtomicInteger n = new AtomicInteger(-1);

  @Override
  public Connection getConnection() throws SQLException {
    if (n.incrementAndGet() < 1)
      return ds.getConnection();
    throw new Unsupported();
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    throw new Unsupported();
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    throw new Unsupported();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    throw new Unsupported();
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    throw new Unsupported();
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    throw new Unsupported();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new Unsupported();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new Unsupported();
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new Unsupported();
  }

  @Override
  public ShardingKeyBuilder createShardingKeyBuilder() throws SQLException {
    throw new Unsupported();
  }

  @Override
  public ConnectionBuilder createConnectionBuilder() throws SQLException {
    throw new Unsupported();
  }
}
