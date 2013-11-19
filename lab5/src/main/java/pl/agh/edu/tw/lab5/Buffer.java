package pl.agh.edu.tw.lab5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: Piotr Turek
 */
public class Buffer<T> {
    private final static Logger LOG = LoggerFactory.getLogger(Buffer.class);

    private final Queue<T> resourceQueue;
    private volatile boolean firstProducerTaken = false;
    private volatile boolean firstConsumerTaken = false;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition firstProducerFree = lock.newCondition();
    private final Condition firstConsumerFree = lock.newCondition();
    private final Condition restOfProducers = lock.newCondition();
    private final Condition restOfConsumers = lock.newCondition();
    private final int resourceCount;
    private volatile int producedCount;

    public Buffer(int maxSize) {
        this.resourceQueue = new ArrayDeque<T>();
        this.resourceCount = maxSize;
        this.producedCount = 0;
    }

    public List<T> consume(int chunkSize) throws InterruptedException {
        List<T> chunk = new ArrayList<T>();
        lock.lock();
        try {
            while (firstConsumerTaken) {
                restOfConsumers.await();
            }
            while (producedCount < chunkSize) {
                firstConsumerTaken = true;
                firstConsumerFree.await();
            }
            producedCount -= chunkSize;
            for (int i = 0; i < chunkSize; i++) {
                chunk.add(resourceQueue.poll());
            }
            firstConsumerTaken = false;
            restOfConsumers.signal();
            firstProducerFree.signal();
        } finally {
            lock.unlock();
        }
        return chunk;
    }

    public void produce(List<T> chunk) throws InterruptedException {
        int chunkSize = chunk.size();
        lock.lock();
        try {
            while (firstProducerTaken) {
                restOfProducers.await();
            }
            while (resourceCount - producedCount < chunkSize) {
                firstProducerTaken = true;
                firstProducerFree.await();
            }
            producedCount += chunkSize;
            resourceQueue.addAll(chunk);
            firstProducerTaken = false;
            restOfProducers.signal();
            firstConsumerFree.signal();
        } finally {
            lock.unlock();
        }
    }

}
