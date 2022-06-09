
package use.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
// import use.log.Log;

/**
 SqlReporter. */
public class SqlReporter implements InvocationHandler {
  private final String db;
  private final Connection conn;


  // private static final Log log = Log.getLog(SqlReporter.class);

  public SqlReporter(String db, Connection conn) {
    this.db = db;
    this.conn = conn;
  }

  @SuppressWarnings("rawtypes")
  public Connection getConnection() {
    Class clazz = conn.getClass();
    return (Connection) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{Connection.class}, this);
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      if (method.getName().equals("prepareStatement")) {
        String info = "db: " + db + " sql: " + args[0];
        System.out.println(info);
      }
      return method.invoke(conn, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }
}




