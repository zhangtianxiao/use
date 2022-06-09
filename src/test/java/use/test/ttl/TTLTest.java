package use.test.ttl;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class TTLTest {
  public static void main(String[] args) {
    Vertx timer = Vertx.vertx();
    
    var context = new TransmittableThreadLocal<String>();

    timer.setTimer(10, (id) -> {
      context.set("1");
      System.out.println("task1, after set: " + context.get() + "  " + Thread.currentThread());

      timer.setTimer(2000, (ignored) -> {
        System.out.println("task1, after wait : " + context.get() + "  " + Thread.currentThread());
      });
    });
    timer.setTimer(1000, (id) -> {
      context.set("2");
      System.out.println("task2, after set : " + context.get() + "  " + Thread.currentThread());
    });
  }
}
