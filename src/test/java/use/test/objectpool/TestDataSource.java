package use.test.objectpool;

import com.jsoniter.any.Any;
import use.jdbc.DataSourceKit;
import use.jdbc.DbKit;
import use.kit.Helper;
import use.kit.ObjectPool;
import use.kit.ResourceMode;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
2022-06-08 05:46:16 main INFO  com.zaxxer.hikari.HikariDataSource:80(HikariDataSource.java:80)
	HikariPool-1 - Starting...
2022-06-08 05:46:16 main INFO  com.zaxxer.hikari.HikariDataSource:82(HikariDataSource.java:82)
	HikariPool-1 - Start completed.
name: hikari, size: 0, time = 19059/ms, hit = 0/c, mis = 0/c
name: hikari, size: 0, time = 17484/ms, hit = 0/c, mis = 0/c
name: hikari, size: 0, time = 17435/ms, hit = 0/c, mis = 0/c
2022-06-08 05:47:10 main INFO  com.zaxxer.hikari.HikariDataSource:350(HikariDataSource.java:350)
	HikariPool-1 - Shutdown initiated...
2022-06-08 05:47:10 main INFO  com.zaxxer.hikari.HikariDataSource:352(HikariDataSource.java:352)
	HikariPool-1 - Shutdown completed.
name: jdbc-datasource-test, size: 24, time = 18292/ms, hit = 0/c, mis = 24/c
name: jdbc-datasource-test, size: 24, time = 17584/ms, hit = 0/c, mis = 0/c
name: jdbc-datasource-test, size: 24, time = 17430/ms, hit = 0/c, mis = 0/c
* */
public class TestDataSource {
  /**
   *
   */
  public static void main(String[] args) throws InterruptedException, SQLException {
    // 最终benchmark时关掉HIT统计
    System.setProperty("ObjectPool-Statistics", "false");
    final ExecutorService executor = Executors.newFixedThreadPool(24);
    final int count = 100000;

    //
    final Any dbOptions = Helper.ENV.get("dataSourceList").get(1);
    DataSource ds = DataSourceKit.newDataSource(dbOptions);
    int sleep = 3;
    // 3 或 0, sleep模拟对象的使用耗时
    Test.pooledTest(executor, count, sleep, "hikari"
      , () -> {
      }
      , () -> {
        try {
          return ds.getConnection();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
      , connection -> {
        try {
          connection.close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }, () -> 0, () -> 0L, () -> 0L);
    Helper.close(ds);

    //
    ObjectPool<Connection> connPool = DbKit.newConnPool(dbOptions, ResourceMode.Concurrent);
    Test.pooledTest(executor, count, sleep, connPool);
    executor.shutdown();
  }


}
