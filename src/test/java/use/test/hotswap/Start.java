package use.test.hotswap;

import use.hotswap.HotSwap;

import java.util.concurrent.atomic.AtomicInteger;

/*
 *  启动后尝试增删 ModifyThisClass 中的方法
 * */
public class Start implements Runnable, AutoCloseable {

  public static void main(String[] args) {
    var hotSwap = new HotSwap<>(Start.class);
    hotSwap
      .addHotSwapClassPrefix("com.abc")
      .start();
  }

  AtomicInteger count = new AtomicInteger(0);
  Thread t = new Thread(() -> {
    while (true) {
      count.incrementAndGet();
      System.out.println(count.get());
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
        return;
      }
    }
  });

  @Override
  public void run() {
    for (var f : ModifyThisClass.class.getDeclaredMethods()) {
      System.out.println(f);
    }
    System.out.println("t: " + t);
    t.start();
  }

  @Override
  public void close() {
    System.out.println("closing");
    t.interrupt();
  }

}






