package use.test.template;

import use.template.Engine;
import use.template.Template;

import java.util.HashMap;

public class Test1 {
  public static void main(String[] args) {
    var s = """
      123
      456
      #if(true)#end
      #(map.put('k','v'))
      #for(i : [1..3])
      i:#(i),for.index: #(for.index)
      #end
      #for(i = 3; i < 6; i++)
         #(i)
      #end
            
      #(map.k) #(map['k'])
      经扩展后, 支持对map.取值
      #[[#(map.k) #(map['k'])]]#
            
      #set(map["k"] = 'kk')
      支持这样赋值
      #[[#set(map["k"] = 'kk')]]#
            
      不支持.赋值
      #[[#set(map.k = 'kk')]]#
            
      不支持.赋值
      #[[#(map.k = 'v')]]#
      """;
    var engine = Engine.byClassPath("");
    HashMap map = new HashMap<>();
    engine.addSharedObject("map", map);
    Template template = engine.getTemplateByString(s);
    String  ret = template.renderToString(new HashMap<>());
    System.out.println(ret);
    System.out.println(map.get("k"));
  }

 /* public <T> void fn(T t){
    Class<?> c1 = t.getClass();
    Class<T> c2 = t.getClass();
  }*/
}
