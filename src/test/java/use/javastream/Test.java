package use.javastream;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Test {
  class N {
    private N(int n) {
    }


  }

  public static void main(String[] args) {
    N[] arr = new N[1];
    //Stream<? extends Class<?>> classStream = Stream.of(arr).map(Object::getClass).mapto;
    IntStream intStream = IntStream.of(1);
    Stream<N[]> stream = intStream.mapToObj(N[]::new);

    Stream<String> stringStream = Stream.of(1, 2, 3).map(it -> "");
    stringStream.mapToInt(Integer::parseInt).sum();
  }
}
