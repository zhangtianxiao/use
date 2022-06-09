package use.test.threads;

import cn.hutool.core.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class Test {
  public static void main(String[] args) {
    var threads = new ArrayList<Thread>();
    var pool = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r);
      threads.add(thread);
      return thread;
    });
    var count = new CountDownLatch(1);
    Runnable r = () -> {
      long x = 0;
      System.out.println("submit!");
      try {
        System.out.println("waiting stop");
        // 一些线程, IO的API, 会因为interuptor而中断
        count.await();
        // 如果是纯计算, 是没办法被中断的
        while (x < 12618832575L) x += 1;
      } catch (Throwable e) {
        System.err.println("Throwable");
        e.printStackTrace();
      }
    };
    pool.execute(r);
    ThreadUtil.sleep(1000);
    for (Thread thread : threads) {
      System.out.println("try stop");
      thread.interrupt();
      //thread.stop();
    }
    ThreadUtil.sleep(2000);
    System.out.println("resubmit");
    pool.execute(r);

  }
}
