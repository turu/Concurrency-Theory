package pl.agh.edu.tw.activeobject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: Piotr Turek
 */
public class ResourceBufferProxyFactory<T> {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Scheduler<T> scheduler;
    private final ResourceBuffer<T> buffer;

    public ResourceBufferProxyFactory(int bufferSize) {
        buffer = new ResourceBuffer<T>(bufferSize);
        scheduler = new Scheduler<T>();
        executor.submit(scheduler);
    }

    public ResourceBufferProxy<T> getProxy() {
        final ResourceBufferProxy<T> proxy = new ResourceBufferProxy<T>(scheduler, buffer);
        return proxy;
    }

}
