package pl.agh.edu.tw.activeobject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Author: Piotr Turek
 */
public class ResourceBufferProxy<T> {
    private final Logger LOG = LoggerFactory.getLogger(ResourceBufferProxy.class);

    private final Scheduler<T> scheduler;
    private final ResourceBuffer<T> buffer;

    ResourceBufferProxy(Scheduler<T> scheduler, ResourceBuffer<T> buffer) {
        this.scheduler = scheduler;
        this.buffer = buffer;
    }


    public IFuture<Void> produce(Collection<T> chunk) {
        final MyFuture<Void> future = new MyFuture<Void>();
        final IMethodRequest methodRequest = new ProduceMethodRequest<T>(chunk, buffer, future);
        scheduler.schedule(methodRequest);
        return future;
    }

    public IFuture<Collection<T>> consume(int chunkSize) {
        final MyFuture<Collection<T>> future = new MyFuture<Collection<T>>();
        final IMethodRequest methodRequest = new ConsumeMethodRequest<T>(chunkSize, buffer, future);
        scheduler.schedule(methodRequest);
        return future;
    }

}
