package use.test.template;

public class Test4 {

}

class Scope {
  Object a;
  Object b;

  Config config;
}

class Config {
}

abstract class Base {
  /**
   fn方法将被许多子类重写, config也要被显示传递很多次
   */
  public abstract void fn(Scope scope, Config config);
}

class Base1 extends Base {

  @Override
  public void fn(Scope scope, Config config) {
    fn(config);
    fn2(config);
  }

  void fn(Config config) {
  }

  void fn2(Config config) {
  }
}

/*
class Baseb extends Base {

  @Override
  public void fn(Scope scope) {
    fn(scope);
    fn2(scope);
  }

  void fn(Scope scope) {
    Config config = scope.config;
  }

  void fn2(Scope scope) {
    Config config = scope.config;
  }
}
*/
