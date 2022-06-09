package use.concurrent.v1;

import cn.hutool.core.thread.ThreadUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.Supplier;

public class Test {
  public static void main(String[] args) throws InterruptedException {
    // ArrayDeque queue = new ArrayDeque<>();
    //ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue<>();
    //LinkedBlockingDeque queue = new LinkedBlockingDeque<>(12);

    final ExecutorService executor = Executors.newFixedThreadPool(24);
    final int count = 1000000;
    final CountDownLatch latch = new CountDownLatch(count);
    final int max = 12;

    //Supplier<Long> supplier = System::currentTimeMillis;
    AtomicLong atomicLong = new AtomicLong();
    Supplier<Long> supplier = atomicLong::get;
    Function<Long, Boolean> recycle = it -> true;

    ArrayDeque<Long> deque = new ArrayDeque<>();
    Supplier<Queue<Long>> queueSupplierForSingleThread = () -> deque;
    ObjectPool_SingleThread<Long> poolForSingleThread = new ObjectPool_SingleThread<>(max, queueSupplierForSingleThread, supplier, recycle);

    ThreadLocal<ArrayDeque<Long>> TL = ThreadLocal.withInitial(() -> new ArrayDeque<>());
    ObjectPool_ThreadLocal<Long> poolForThreadLocal = new ObjectPool_ThreadLocal<>(max, TL::get, supplier, recycle);

    ConcurrentLinkedQueue<Long> concurrentQueue = new ConcurrentLinkedQueue<>();
    ObjectPool_Concurrent<Long> poolForConcurrent = new ObjectPool_Concurrent<>(max, () -> concurrentQueue, supplier, recycle);


    //
    ObjectPool<Long> pool = poolForConcurrent;
    Runnable r = () -> {
      Long v = pool.get();
      ThreadUtil.sleep(3);
      pool.recycle(v);
      latch.countDown();
    };

    long begin = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      executor.execute(r);
    }

    latch.await();
    long time = System.currentTimeMillis() - begin;
    System.out.printf("mode: %s, size: %s, time = %d/ms, hit = %s/c, mis = %s/c\n", "concurrent", pool.q.get().size(), time, pool.HIT_COUNT, pool.MISS_COUNT);
  }
}

abstract class ObjectPool<T> {
  public final LongAdder HIT_COUNT = new LongAdder();
  public final LongAdder MISS_COUNT = new LongAdder();

  final int limit;

  /**
   存放对象的队列
   */
  final Supplier<Queue<T>> q;

  /**
   无可用对象时生成新对象
   */
  final Supplier<T> maker;
  /**
   返回true时添加到队列
   */
  @Nullable
  final Function<T, Boolean> cleaner;

  ObjectPool(int limit, Supplier<Queue<T>> q, Supplier<T> maker, @Nullable Function<T, Boolean> cleaner) {
    this.limit = limit;
    this.q = q;
    this.maker = maker;
    this.cleaner = cleaner;
  }

  /**
   由子类实现计数器
   */
  public abstract boolean incrementMakeCount();

  public T get() {
    T t = q.get().poll();
    // 为空, 创建新对象
    if (t == null) {
      // 需要在这个位置做limit判断的逻辑
      if (incrementMakeCount()) {
        MISS_COUNT.add(1);
        return maker.get();
      } else {
        try {
          //  休眠
          Thread.sleep(0, 1);
          // 递归获取
          return get();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
    HIT_COUNT.add(1);
    return t;
  }

  /**
   @return 对象是否被添加到队列
   */
  public boolean recycle(T t) {
    if (cleaner != null && cleaner.apply(t)) {
      return q.get().offer(t);
    }
    return false;
  }
}

class ObjectPool_Concurrent<T> extends ObjectPool<T> {
  final AtomicInteger latch = new AtomicInteger(limit);

  ObjectPool_Concurrent(int limit, Supplier<Queue<T>> q, Supplier<T> maker, @Nullable Function<T, Boolean> cleaner) {
    super(limit, q, maker, cleaner);
  }

  @Override
  public boolean incrementMakeCount() {
    if (limit < 0)
      return true;
    return latch.decrementAndGet() > 0;
  }

}

class ObjectPool_ThreadLocal<T> extends ObjectPool<T> {
  int latch = limit;

  ObjectPool_ThreadLocal(int limit, Supplier<Queue<T>> q, Supplier<T> maker, @Nullable Function<T, Boolean> cleaner) {
    super(limit, q, maker, cleaner);
  }

  @Override
  public boolean incrementMakeCount() {
    if (limit < 0)
      return true;
    boolean b = latch > 0;
    if (b)
      latch -= 1;
    return b;
  }
}

class ObjectPool_SingleThread<T> extends ObjectPool<T> {
  int latch = limit;

  ObjectPool_SingleThread(int limit, Supplier<Queue<T>> q, Supplier<T> maker, @Nullable Function<T, Boolean> cleaner) {
    super(limit, q, maker, cleaner);
  }

  @Override
  public boolean incrementMakeCount() {
    if (limit < 0)
      return true;
    boolean b = latch > 0;
    if (b)
      latch -= 1;
    return b;
  }
}
