package pl.agh.edu.tw.activeobject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Author: Piotr Turek
 */
public class ResourceBufferProxy<T> {

    private final Scheduler<T> scheduler;
    private final ResourceBuffer<T> buffer;

    ResourceBufferProxy(Scheduler<T> scheduler, ResourceBuffer<T> buffer) {
        this.scheduler = scheduler;
        this.buffer = buffer;
    }


    public IFuture<Void> produce(Collection<T> chunk) {
        final IFuture<Void> future = new Future<Void>();
        final IMethodRequest methodRequest = new ProduceMethodRequest<T>(chunk, buffer, future);
        scheduler.schedule(methodRequest);
        return future;
    }

    public IFuture<Collection<T>> consume(int chunkSize) {
        final IFuture<Collection<T>> future = new Future<Collection<T>>();
        final IMethodRequest methodRequest = new ConsumeMethodRequest<T>(chunkSize, buffer, future);
        scheduler.schedule(methodRequest);
        return future;
    }

    class Future<T> implements IFuture<T> {

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

        @Override
        public synchronized void set(T result) {
            value = result;
            available = true;
            this.notify();
        }
    }

}
