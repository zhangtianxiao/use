package use.jdbc;

import org.jetbrains.annotations.Nullable;
import use.kit.ResourceMode;

import java.sql.Connection;

import static use.kit.ResourceMode.Concurrent;
import static use.kit.ResourceMode.SingleThread;

public abstract class ConnectionHolder {

  public abstract Connection get();

  public abstract void set(Connection con);

  public abstract void remove();


  public static ConnectionHolder create(ResourceMode mode) {
    if (mode.equals(SingleThread))
      return new ConnectionHolder_Single();
    return new ConnectionHolder_ThreadLocal();
  }

  public static ConnectionHolder create() {
    return create(Concurrent);
  }


  public static class ConnectionHolder_Single extends ConnectionHolder {
    Connection con;

    @Nullable
    public Connection get() {
      return con;
    }

    public void set(Connection con) {
      this.con = con;
    }

    public void remove() {
      this.con = null;
    }
  }

  public static class ConnectionHolder_ThreadLocal extends ConnectionHolder {
    private final ThreadLocal<Connection> conn_tl = new ThreadLocal<Connection>();

    @Nullable
    @Override
    public Connection get() {
      return conn_tl.get();
    }

    @Override
    public void set(Connection con) {
      conn_tl.set(con);
    }

    @Override
    public void remove() {
      conn_tl.remove();
    }
  }

}