package use.test.template;

import use.beans.Beans;
import use.beans.FieldDesc;
import use.template.Engine;
import use.test.mb.Test;

import java.util.HashMap;

public class Test3 {
  public static void main(String[] args) {
    Beans.me.register(FieldDesc.create(Test.class, "id", (o, value) -> o.id = (Long) value, o -> o.id));

    Test test = new Test();
    // #set(test.id  = 123L)
    var s = """
      #set(test['id']  = 123L)
      """;
    var engine = Engine.byClassPath("");
    engine.addSharedObject("test", test);

    var template = engine.getTemplateByString(s);
    var ret = template.renderToString(new HashMap<>());
    System.out.println(ret);
    System.out.println(test.id);
  }

 /* public <T> void fn(T t){
    Class<?> c1 = t.getClass();
    Class<T> c2 = t.getClass();

    String a = "12";
    Class<? extends String> aClass = a.getClass();
  }*/
}
