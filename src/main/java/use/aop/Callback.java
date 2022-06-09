package use.aop;

@FunctionalInterface
public interface Callback {
  Object invoke(Invocation inv) throws Throwable;
}
