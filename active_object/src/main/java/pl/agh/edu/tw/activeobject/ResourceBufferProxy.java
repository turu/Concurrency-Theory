package pl.agh.edu.tw.activeobject;

import java.util.Collection;
import java.util.concurrent.Future;

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
        final FutureImpl<Void> future = new FutureImpl<Void>();
        final IMethodRequest methodRequest = new ProduceMethodRequest<T>(chunk, buffer, future);
        scheduler.schedule(methodRequest);
        return future;
    }

    public IFuture<Collection<T>> consume(int chunkSize) {
        final FutureImpl<Collection<T>> future = new FutureImpl<Collection<T>>();
        final IMethodRequest methodRequest = new ConsumeMethodRequest<T>(chunkSize, buffer, future);
        scheduler.schedule(methodRequest);
        return future;
    }

}
