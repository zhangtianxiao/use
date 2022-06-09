
package use.jdbc;

import java.io.IOException;
import java.sql.SQLException;

/**
 ActiveRecordException */
public class ActiveRecordException extends RuntimeException {

  private static final long serialVersionUID = 342820722361408621L;

  public ActiveRecordException(String message) {
    super(message);
  }

  public ActiveRecordException(Throwable cause) {
    super(cause);
  }

  public ActiveRecordException(SQLException cause) {
    super(cause);
  }

  public static ActiveRecordException wrap(SQLException e) {
    return new ActiveRecordException(e);
  }

  /**
   区分SQLException, 用以追踪
   */
  public static ActiveRecordException wrapEx(Throwable e) {
    if (e instanceof ActiveRecordException e1) return e1;
    return new ActiveRecordException(e);
  }

  public static ActiveRecordException wrapEx(IOException e) {
    return new ActiveRecordException(e);
  }
}










