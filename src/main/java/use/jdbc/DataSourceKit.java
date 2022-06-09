package use.jdbc;

import com.jsoniter.any.Any;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceKit {
  public static DataSource newDataSource(Any JDBC) {
    String url = JDBC.toString("url");
    String username = JDBC.toString("username");
    String password = JDBC.toString("password");
    int max = JDBC.toInt("max");
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(url);
    config.setUsername(username);
    config.setPassword(password);
    config.setInitializationFailTimeout(2000);
    config.setConnectionTimeout(2000);
    config.setMaximumPoolSize(max);

    DataSource dataSource = new HikariDataSource(config);
    return dataSource;
  }
}
