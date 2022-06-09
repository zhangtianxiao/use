package use.kit;

import cn.hutool.system.SystemUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ObjectPool<T> {
  /**
   默认会统计HIT_COUNT
   */
  public static final boolean Statistics = SystemUtil.getBoolean("ObjectPool-Statistics", true);

  public <R> R use(Function<T, R> fn) {
    T t = get();
    try {
      return fn.apply(t);
    } finally {
      recycle(t);
    }
  }

  public final LongAdder HIT_COUNT = new LongAdder();
  public final LongAdder MISS_COUNT = new LongAdder();

  public final String name;
  public final int limit;

  /**
   存放对象的队列
   */
  public final Supplier<Queue<T>> q;

  /**
   无可用对象时生成新对象
   */
  final Supplier<T> maker;
  /**
   返回true时添加到队列
   */
  @Nullable
  final Function<T, Boolean> cleaner;

  ObjectPool(String name, int limit, Supplier<Queue<T>> q, Supplier<T> maker, @Nullable Function<T, Boolean> cleaner) {
    this.name = name;
    this.limit = limit;
    this.q = q;
    this.maker = maker;
    this.cleaner = cleaner;
  }

  /**
   由子类实现计数器
   */
  public abstract boolean incrementMakeCount();

  @Nullable
  public T tryGet() {
    Queue<T> queue = q.get();
    if (queue != null) {
      T t = queue.poll();
      if (t != null) {
        if (Statistics)
          HIT_COUNT.add(1);
        return t;
      }
    }
    return null;
  }

  @NotNull
  public T get() {
    T t = tryGet();
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


  public static <T> ObjectPool<T> create(String name, Supplier<T> maker, Function<T, Boolean> disposer, int max) {
    return create(name, maker, disposer, max, ResourceMode.Concurrent);
  }

  public static <T> ObjectPool<T> create(String name, Supplier<T> maker, Consumer<T> disposer, int max, ResourceMode mode) {
    return create(name, maker, it -> {
      disposer.accept(it);
      return true;
    }, max, mode);
  }

  public static <T> ObjectPool<T> create(String name, Supplier<T> maker, Function<T, Boolean> disposer, int max, ResourceMode mode) {
    return switch (mode) {
      case SingleThread -> {
        Queue<T> val = new ArrayDeque<>();
        yield new ObjectPool_SingleThread<T>(name, max, () -> val, maker, disposer);
      }
      case Concurrent -> {
        /*Supplier<Queue<T>> queue = ArrayDeque::new;
        yield new ObjectPool_Default<T>(mode, maker, disposer, max, queue);*/
        Queue<T> val = new ConcurrentLinkedQueue<T>();
        yield new ObjectPool_Concurrent<T>(name, max, () -> val, maker, disposer);
      }
    };
  }
}

class ObjectPool_Concurrent<T> extends ObjectPool<T> {
  final AtomicInteger latch = new AtomicInteger(limit);

  ObjectPool_Concurrent(String name, int limit, Supplier<Queue<T>> q, Supplier<T> maker, @Nullable Function<T, Boolean> cleaner) {
    super(name, limit, q, maker, cleaner);
  }

  @Override
  public boolean incrementMakeCount() {
    if (limit < 0)
      return true;
    if (latch.get() <= 0)
      return false;
    return latch.getAndDecrement() > 0;
  }

  final java.util.concurrent.Semaphore semaphore = new Semaphore(0, true);


  @Override
  public @Nullable T tryGet() {
    //if (Statistics) System.out.println("tryAcquire...");
    boolean b = semaphore.tryAcquire();
    if (b) {
      T t = super.tryGet();
      // if (t == null) System.out.println("嗯?");
      return t;
    } else
      return null;
  }

  AtomicInteger recursive = new AtomicInteger(0);

  @Override
  @NotNull
  public T get() {
    T t = tryGet();
    // 为空, 创建新对象
    if (t == null) {
      // 没有超出最大限制
      if (incrementMakeCount()) {
        MISS_COUNT.add(1);
        return maker.get();
      } else {
        try {
          // 等待
          // if (Statistics) System.out.println("acquire...");
          semaphore.acquire();
          // 递归获取
          //int i = recursive.incrementAndGet();
          //System.out.println("递归获取 " + i + " " + Thread.currentThread());
          // 经信号量确认,这里不可能返回null
          T t1 = super.tryGet();
          //if (t1 == null) System.out.println("嗯?");
          //System.out.println("递归获得 " + i + " " + Thread.currentThread());
          return t1;
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return t;
  }

  /**
   @return 对象是否被添加到队列
   */
  @Override
  public boolean recycle(T t) {
    if (cleaner != null && cleaner.apply(t)) {
      boolean putted = q.get().offer(t);
      if (putted) {
        // if (Statistics) System.out.println("release...");
        semaphore.release();
      } else {
        // if (Statistics) System.out.println("does not release...");
      }
      return putted;
    }
    return false;
  }
}

class ObjectPool_SingleThread<T> extends ObjectPool<T> {
  int latch = limit;

  ObjectPool_SingleThread(String name, int limit, Supplier<Queue<T>> q, Supplier<T> maker, @Nullable Function<T, Boolean> cleaner) {
    super(name, limit, q, maker, cleaner);
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
