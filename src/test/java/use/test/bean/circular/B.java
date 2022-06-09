package use.test.bean.circular;

import org.springframework.stereotype.Component;

@Component
public class B {
  final A a;

  public B(A a) {
    this.a = a;
  }
}
