
package use.jdbc;


@FunctionalInterface
public interface IAtom {
  boolean run() throws Throwable;
}