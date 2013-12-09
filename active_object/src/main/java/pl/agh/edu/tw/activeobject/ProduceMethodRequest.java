package pl.agh.edu.tw.activeobject;

import java.util.Collection;

/**
 * Author: Piotr Turek
 */
public class ProduceMethodRequest<T> implements IMethodRequest {

    private final Collection<T> chunk;
    private final ResourceBuffer<T> buffer;
    private final MyFuture<Void> future;
    private final long priority;

    public ProduceMethodRequest(Collection<T> chunk, ResourceBuffer<T> buffer, MyFuture<Void> future) {
        this.chunk = chunk;
        this.buffer = buffer;
        this.future = future;
        priority = System.currentTimeMillis();
    }

    @Override
    public boolean guard() {
        return buffer.canProduce(chunk.size());
    }

    @Override
    public void execute() {
        buffer.produce(chunk);
        future.set(null);
    }

    @Override
    public long getPriority() {
        return priority;
    }
}
