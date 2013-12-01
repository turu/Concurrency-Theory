package pl.agh.edu.tw.activeobject;

/**
 * Author: Piotr Turek
 */
public interface IFuture<T> {
    boolean isAvailable();
    T get() throws InterruptedException;
}
