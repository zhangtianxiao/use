package use.jdbc.graph;

import use.jdbc.Db;

public class AlreadyCommitException  extends RollbackException {
  public AlreadyCommitException(Db db) {
    super(db,null);
  }
}
