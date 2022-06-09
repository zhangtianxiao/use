package use.test.vertx;

import cn.hutool.core.thread.ThreadUtil;
import io.vertx.core.Vertx;
import use.kit.Helper;
import use.kit.VertxKit;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class TimerTest {
  public static void main(String[] args) throws InterruptedException, ExecutionException {
    Vertx vertx = Vertx.vertx();
    vertx.setPeriodic(1000, it -> {
      System.out.println(new Date());
    });

    Thread thread = new Thread(() -> {
      ThreadUtil.sleep(3000);
      vertx.close();
    });
    thread.start();
    new CountDownLatch(1).await();

  }
}
