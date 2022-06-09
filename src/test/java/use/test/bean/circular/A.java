package use.test.bean.circular;

import org.springframework.stereotype.Component;

@Component
public class A {
  final B b;

  public A(B b) {
    this.b = b;
  }
}
