package use.concurrent.v3;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 Synchronization implementation for semaphore.  Uses AQS state
 to represent permits. Subclassed into fair and nonfair
 versions.
 */


public abstract class Sync extends AbstractQueuedSynchronizer {
  private static final long serialVersionUID = 1192457210091910933L;

  Sync(int permits) {
    setState(permits);
  }

  public void reSetState(int n) {
    setState(n);
  }

  final int getPermits() {
    return getState();
  }

  final int nonfairTryAcquireShared(int acquires) {
    for (; ; ) {
      int available = getState();
      int remaining = available - acquires;
      if (remaining < 0 ||
        compareAndSetState(available, remaining))
        return remaining;
    }
  }

  protected final boolean tryReleaseShared(int releases) {
    for (; ; ) {
      int current = getState();
      int next = current + releases;
      if (next < current) // overflow
        throw new Error("Maximum permit count exceeded");
      if (compareAndSetState(current, next))
        return true;
    }
  }

  final void reducePermits(int reductions) {
    for (; ; ) {
      int current = getState();
      int next = current - reductions;
      if (next > current) // underflow
        throw new Error("Permit count underflow");
      if (compareAndSetState(current, next))
        return;
    }
  }

  final int drainPermits() {
    for (; ; ) {
      int current = getState();
      if (current == 0 || compareAndSetState(current, 0))
        return current;
    }
  }

}