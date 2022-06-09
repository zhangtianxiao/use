package use.test.objectpool;

import cn.hutool.core.thread.ThreadUtil;
import use.kit.ObjectPool;
import use.kit.ResourceMode;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Test {
  public static void main(String[] args) throws InterruptedException {
    // 最终benchmark时关掉HIT统计
    System.setProperty("ObjectPool-Statistics", "true");

    final ExecutorService executor = Executors.newFixedThreadPool(24);

    final int max = 12;

    AtomicLong atomicLong = new AtomicLong();

    // sleep调大点, 单线程就废了
    int sleep = 0;

    // 合适的存取次数
    final int count = sleep == 0 ? 10000_00 : 10000;

    ObjectPool<Long> poolForConcurrent = ObjectPool.create("多线程", atomicLong::incrementAndGet, it -> true, max, ResourceMode.Concurrent);
    pooledTest(executor, count, sleep, poolForConcurrent);

    long[] longs = new long[1];

    ObjectPool<Long> poolForSingleThread = ObjectPool.create("单线程", () -> longs[0]++, it -> true, max, ResourceMode.SingleThread);
    pooledTest(Executors.newSingleThreadExecutor(), count, sleep, poolForSingleThread);

    executor.shutdown();
  }

  public static <T> void pooledTest(ExecutorService executor, int count, int sleep, ObjectPool<T> pool) throws InterruptedException {
    pooledTest(executor, count, sleep, pool.name, () -> {
      pool.HIT_COUNT.reset();
      pool.MISS_COUNT.reset();
    }, pool::get, pool::recycle, pool.q.get()::size, pool.HIT_COUNT::longValue, pool.MISS_COUNT::longValue);
  }

  public static <T> void pooledTest(
    ExecutorService executor,
    int count,
    int sleep,
    String name,
    Runnable reset,
    Supplier<T> getter,
    Consumer<T> disposer,
    Supplier<Integer> size, Supplier<Long> hit, Supplier<Long> miss) throws InterruptedException {
    // 跑10轮
    for (int j = 0; j < 3; j++) {
      final CountDownLatch latch = new CountDownLatch(count);
      Runnable r = () -> {
        // 获取
        T v = getter.get();
        // 回收
        if (sleep != 0)
          ThreadUtil.sleep(sleep);
        disposer.accept(v);
        latch.countDown();
      };
      reset.run();

      long begin = System.currentTimeMillis();
      for (int i = 0; i < count; i++) {
        executor.execute(r);
      }

      latch.await();
      long time = System.currentTimeMillis() - begin;
      System.out.printf("name: %s, size: %s, time = %d/ms, hit = %s/c, mis = %s/c\n", name, size.get(), time, hit.get(), miss.get());
    }

  }
}

