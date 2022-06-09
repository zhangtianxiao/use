package use.kit;

public class Func {
  @FunctionalInterface
  public interface Func3<P1, P2, P3, R> {
    R apply(P1 p1, P2 p2, P3 p3);
  }

  public interface Func4<P1, P2, P3,P4, R> {
    R apply(P1 p1, P2 p2, P3 p3,P4 p4);
  }

  public interface Func5<P1, P2, P3, P4,P5, R> {
    R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
  }

  public interface Func6<P1, P2, P3, P4, P5,P6, R> {
    R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5,P6 p6);
  }

  @FunctionalInterface
  public interface Consumer3<P1, P2, P3> {
    void call(P1 p1, P2 p2, P3 p3);
  }

  @FunctionalInterface
  public interface VoidFunc1<P> {
    void call(P parameter);
  }

  @FunctionalInterface
  public interface VoidFunc2<P1, P2> {
    void call(P1 p1, P2 p2);
  }


  /*public interface IoRunnable{
    void run() throws IOException;
  }

  public interface IoConsumer<T> {
    void accept(T t) throws IOException;
  }*/
}
