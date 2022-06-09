package use.jdbc.graph;

import use.jdbc.Db;

public class RollbackException extends RuntimeException {
  final Db db;

  public RollbackException(Db db, Throwable e) {
    super(e);
    this.db = db;
  }
}