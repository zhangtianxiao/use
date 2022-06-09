package use.test.longmap;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ReflectUtil;
import io.netty.util.collection.LongObjectHashMap;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 --add-opens java.base/java.util=ALL-UNNAMED
 读取速度, LongHashMap vs HashMap
 loopN 越大, longMap读取相对更快
 除了longHashMap本身设计能够减少内存占用
 实际使用中, long值作为key, 更能节省内存
 */
public class LongMapReadTest {
  static int loopN = 1000000;
  static int loopM = 10;

  public static void main(String[] args) throws IllegalAccessException {
    TimeInterval timer = DateUtil.timer();
    Long v = 0L;
    // 提前准备好key, 避免装箱开销
    Long[] values = new Long[loopM * loopN];
    for (int i = 0; i < values.length; i++) {
      values[i] = (long) i;
    }
    LongObjectHashMap<Long> longMap = new LongObjectHashMap<>();
    for (int i = 0; i < 10; i++) {
      v = getaLong(timer, v, values, longMap);
    }
    System.out.println(v);
  }

  private static Long getaLong(TimeInterval timer, Long v, Long[] values, LongObjectHashMap<Long> longMap) throws IllegalAccessException {
    for (int m = 1; m <= loopM; m++) {
      int n = loopN * m;
      // 写
      for (int i = 0; i < n; i++) {
        longMap.put(values[i], v);
      }

      // 读
      timer.start();
      for (int i = 0; i < n; i++) {
        v = longMap.get(values[i]);
      }
      long longMapElapsed = timer.intervalMs();


      // 写
      HashMap<Long, Long> hashMap = new HashMap<>();
      for (int i = 0; i < n; i++) {
        hashMap.put(values[i], v);
      }

      // 读
      timer.restart();
      for (int i = 0; i < n; i++) {
        v = hashMap.get(values[i]);
      }
      long hashMapElapsed = timer.intervalMs();

      if (m == 0) {
        Field table = ReflectUtil.getField(HashMap.class, "table");
        table.setAccessible(true);
        Object o = table.get(hashMap);
        System.out.println(Array.getLength(o));
      }

    }
    return v;
  }

}
