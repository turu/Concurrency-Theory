package pl.agh.edu.tw.lab3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: Piotr Turek
 */
public class AsynchronousProducersConsumersMonitor<T> {
    private final static Logger LOG = LoggerFactory.getLogger(AsynchronousProducersConsumersMonitor.class);

    private final ReentrantLock freeQueueLock = new ReentrantLock();
    private final ReentrantLock fullQueueLock = new ReentrantLock();
    private final Condition freeNotEmpty = freeQueueLock.newCondition();
    private final Condition freeNotFull = freeQueueLock.newCondition();
    private final Condition fullNotEmpty = fullQueueLock.newCondition();
    private final Condition fullNotFull = fullQueueLock.newCondition();

    private final Queue<Resource<T>> freeQueue = new ArrayDeque<Resource<T>>();
    private final Queue<Resource<T>> fullQueue = new ArrayDeque<Resource<T>>();
    private final int resourceCount;

    public AsynchronousProducersConsumersMonitor(int resourceCount) {
        this.resourceCount = resourceCount;
    }


    public Resource<T> produceBegin() throws InterruptedException {
        Resource<T> resource = null;
        freeQueueLock.lock();
        try {
            while (freeQueue.isEmpty()) {
                freeNotEmpty.await();
            }
            resource = freeQueue.poll();
        } finally {
            freeQueueLock.unlock();
        }
        freeNotFull.signal();
        resource.setState(ResourceState.IN_PRODUCTION);
        return resource;
    }

    public void produceEnd(Resource<T> resource) throws InterruptedException {
        resource.setState(ResourceState.FULL);
        fullQueueLock.lock();
        try {
            while (fullQueue.size() == resourceCount) {
                fullNotFull.await();
            }
            fullQueue.add(resource);
        } finally {
            fullQueueLock.unlock();
        }
        fullNotEmpty.signal();
    }

    public Resource<T> consumeBegin() throws InterruptedException {
        Resource<T> resource = null;
        fullQueueLock.lock();
        try {
            while (fullQueue.isEmpty()) {
                fullNotEmpty.await();
            }
            resource = fullQueue.poll();
        } finally {
            fullQueueLock.unlock();
        }
        fullNotFull.signal();
        resource.setState(ResourceState.IN_CONSUMPTION);
        return resource;
    }

    public void consumeEnd(Resource<T> resource) throws InterruptedException {
        resource.setState(ResourceState.FREE);
        freeQueueLock.lock();
        try {
            while (freeQueue.size() == resourceCount) {
                freeNotFull.await();
            }
            freeQueue.add(resource);
        } finally {
            freeQueueLock.unlock();
        }
        freeNotEmpty.signal();
    }

    public int getResourceCount() {
        return resourceCount;
    }
}
