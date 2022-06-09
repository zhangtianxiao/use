package use.jdbc.graph;

import java.util.List;

public class MergedRollbackException extends RuntimeException {
  final List<RollbackException> errors;

  public MergedRollbackException(Throwable base, List<RollbackException> errors) {
    super(base);
    this.errors = errors;
  }
}


