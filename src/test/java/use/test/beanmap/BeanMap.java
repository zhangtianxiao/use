package use.test.beanmap;

import use.test.mb.Test;

import java.util.HashMap;
import java.util.Map;

public class BeanMap {
  //public static ConcurrentHashMap<Class<?>,>
  public static Map<String, Object> from(Object o) {
    HashMap<Object, Object> map = new HashMap<>();

    return null;
  }

  // 未实现
  @Deprecated
  public static Map<String, Object> immutable(Object o) {
    if (o == null) return Map.of();
    return null;
  }

  public static Map<String, Object> both(Object o) {
    if (o == null) return Map.of();
    return null;
  }

  public static void test() {
    Test test = new Test();
    test.guid = "aabb";
    org.springframework.cglib.beans.BeanMap map = org.springframework.cglib.beans.BeanMap.create(test);
    System.out.println(map.get("guid"));
    // spring的beanMap, 对map赋值 能够映射到原始bean
    map.put("id", 1L);
    System.out.println(test.id);
    // 不存在的字段 被忽略了, 不会报错
    map.put("other", 1L);
  }

  public static void test2() {
    Test test = new Test();
    test.guid = "aabb";
  }

  public static void main(String[] args) {
    test();
  }

}