package use.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnSupplier<T> {
  T apply(Connection conn) throws SQLException;
}
