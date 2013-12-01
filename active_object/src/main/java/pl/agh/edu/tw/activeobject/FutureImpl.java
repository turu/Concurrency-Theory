package pl.agh.edu.tw.activeobject;

/**
 * Author: Piotr Turek
 */
class FutureImpl<T> implements IFuture<T> {

    private T value;
    private volatile boolean available = false;

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public synchronized T get() throws InterruptedException {
        while (!available) {
            this.wait();
        }
        return value;
    }

    public synchronized void set(T result) {
        value = result;
        available = true;
        this.notify();
    }
}
