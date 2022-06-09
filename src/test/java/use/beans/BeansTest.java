package use.beans;

import use.test.mb.Test;

public class BeansTest {
  public static void main(String[] args) throws ClassNotFoundException {
    BeansRegister cls = Beans.auto(Test.class);
    cls.register(Beans.me);
  }
}