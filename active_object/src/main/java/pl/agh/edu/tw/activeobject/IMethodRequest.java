package pl.agh.edu.tw.activeobject;

/**
 * Author: Piotr Turek
 */
public interface IMethodRequest {
    boolean guard();
    void execute();
    long getPriority();
}
