package use.kit.threadsafe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;

public abstract class ThreadSafeInt {
  public static ThreadSafeInt create(int n) {
    return new ByAtomicInteger(n);
  }

  public static ThreadSafeInt create() {
    return create(0);
  }

  public abstract int add(int n);

  public abstract boolean add(int n, int old);

  public abstract int sub(int n);

  public abstract boolean sub(int n, int old);

  public abstract int mul(int n);

  public abstract boolean mul(int n, int old);

  public abstract int div(int n);

  public abstract boolean div(int n, int old);

  public abstract void reset(int n);

  /**
   线程安全的加减乘除, 基于AtomicInteger实现
   */
  private static class ByAtomicInteger extends ThreadSafeInt {
    final AtomicInteger n;

    ByAtomicInteger(int n) {
      this.n = new AtomicInteger(n);
    }


/*    public int div(int n) {
      int old = this.n.get();
      int v = old / n;
      this.n.compareAndSet(old, v);
      return v;
    }*/

    static final IntBinaryOperator ADD = (curr, x) -> curr + x;

    public int add(int n) {
      return this.n.accumulateAndGet(n, ADD);
    }

    /**
     @return boolean 是否更新成功
     */
    public boolean add(int n, int old) {
      return this.n.compareAndSet(old, old + n);
    }

    static final IntBinaryOperator SUB = (curr, x) -> curr - x;

    public int sub(int n) {
      return this.n.accumulateAndGet(n, SUB);
    }

    /**
     @return boolean 是否更新成功
     */
    public boolean sub(int n, int old) {
      return this.n.compareAndSet(old, old - n);
    }


    static final IntBinaryOperator MUL = (curr, x) -> curr * x;

    public int mul(int n) {
      return this.n.accumulateAndGet(n, MUL);
    }

    /**
     @return boolean 是否更新成功
     */
    public boolean mul(int n, int old) {
      return this.n.compareAndSet(old, old * n);
    }

    static final IntBinaryOperator DIV = (curr, x) -> curr / x;

    public int div(int n) {
      return this.n.accumulateAndGet(n, DIV);
    }

    /**
     @return boolean 是否更新成功
     */
    public boolean div(int n, int old) {
      return this.n.compareAndSet(old, old / n);
    }


    /**
     重置值
     */
    @Override
    public void reset(int n) {
      this.n.getAndSet(n);
    }
  }

  /**
   * LongAddr按其文档所述, 更适合用于统计场景而非细粒度的同步控制
   * */

}

/**
 并发编程似乎是极其麻烦的一件事
 AtomicInteger n = 1

 Atomic只能保证 incre/decre是原子的

 thread_1{
 var a = n.get();
 // ...这里是耗时操作, cpu轮到thread_2执行
 // 仍不可避免的存在线程安全问题
 n.set(a*2);

 }

 这个场景应该用compareAndSet

 thread_2{
 var a = n.get();
 n.set(a*2);
 }
 */