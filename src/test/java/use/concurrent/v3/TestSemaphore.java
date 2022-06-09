package use.concurrent.v3;

public class TestSemaphore {
  public static void main(String[] args) throws InterruptedException {
    Semaphore semaphore = new Semaphore(1);

    semaphore.acquire();
    System.out.println("get");
    semaphore.acquire();
    System.out.println("get");

    System.out.println(semaphore.release());
    System.out.println(semaphore.release());

    System.out.println(semaphore.release(1));
    System.out.println(semaphore.release(1));

    System.out.println(semaphore.availablePermits());

  }
}
