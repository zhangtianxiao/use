package use.test.func;

import java.util.function.Function;
import java.util.function.Supplier;

public class Test {
  public static void main(String[] args) {
    Function<Long, Long> fn = it -> it;
    Long n = 100L;
    Supplier<Long> su = () -> fn.apply(n);

    Function<String, Long> compose = fn.compose(Long::parseLong);
    Function<Long, Long> fn2 = fn.andThen(it -> it * 2);
  }
}
