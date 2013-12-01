package pl.agh.edu.tw.activeobject;

import java.util.Collection;

/**
 * Author: Piotr Turek
 */
public class ConsumeMethodRequest<T> implements IMethodRequest {

    private final int chunkSize;
    private final ResourceBuffer<T> buffer;
    private final FutureImpl<Collection<T>> future;
    private final long priority;

    public ConsumeMethodRequest(int chunkSize, ResourceBuffer<T> buffer, FutureImpl<Collection<T>> future) {
        this.chunkSize = chunkSize;
        this.buffer = buffer;
        this.future = future;
        priority = System.currentTimeMillis();
    }

    @Override
    public boolean guard() {
        return buffer.canConsume(chunkSize);
    }

    @Override
    public void execute() {
        future.set(buffer.consume(chunkSize));
    }

    @Override
    public long getPriority() {
        return priority;
    }
}
