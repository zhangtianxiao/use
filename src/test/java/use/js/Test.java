package use.js;

public class Test {
  public static class Component {
    int a;
    int b;
  }

  /**
   怎么编译成
   {
   fn(obj){
   return obj.a+obj.b
   }
   }
   */
  public int fn(Component component) {
    return component.a + component.b;
  }
}
