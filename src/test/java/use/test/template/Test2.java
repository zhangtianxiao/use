package use.test.template;

import use.template.Engine;

import java.util.HashMap;

public class Test2 {
  public static void main(String[] args) {
    // #(map.put('k','v'))
    var s  = """
      #(map.k = 'v')
      """;
    var engine = Engine.byClassPath("");
    var map = new HashMap<>();
    engine.addSharedObject("map",map);
    var template = engine.getTemplateByString(s);
    var ret = template.renderToString(new HashMap<>());
    System.out.println(ret);
    System.out.println(map.get("k"));
  }

 /* public <T> void fn(T t){
    Class<?> c1 = t.getClass();
    Class<T> c2 = t.getClass();
  }*/
}
