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
public class AsynchronousResourceBuffer<T> {
    private final static Logger LOG = LoggerFactory.getLogger(AsynchronousResourceBuffer.class);

    private final ReentrantLock freeQueueLock = new ReentrantLock();
    private final ReentrantLock fullQueueLock = new ReentrantLock();
    private final Condition freeNotEmpty = freeQueueLock.newCondition();
    private final Condition freeNotFull = freeQueueLock.newCondition();
    private final Condition fullNotEmpty = fullQueueLock.newCondition();
    private final Condition fullNotFull = fullQueueLock.newCondition();

    private final Queue<Resource<T>> freeQueue = new ArrayDeque<Resource<T>>();
    private final Queue<Resource<T>> fullQueue = new ArrayDeque<Resource<T>>();
    private final int resourceCount;

    public AsynchronousResourceBuffer(int resourceCount) {
        this.resourceCount = resourceCount;
        initResources(resourceCount);
    }

    public Resource<T> produceBegin() throws InterruptedException {
        Resource<T> resource = null;
        freeQueueLock.lock();
        try {
            while (freeQueue.isEmpty()) {
                freeNotEmpty.await();
            }
            resource = freeQueue.poll();
            freeNotFull.signal();
        } finally {
            freeQueueLock.unlock();
        }
        resource.setState(ResourceState.IN_PRODUCTION);
        resource.setValue(null);
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
            fullNotEmpty.signal();
        } finally {
            fullQueueLock.unlock();
        }
    }

    public Resource<T> consumeBegin() throws InterruptedException {
        Resource<T> resource = null;
        fullQueueLock.lock();
        try {
            while (fullQueue.isEmpty()) {
                fullNotEmpty.await();
            }
            resource = fullQueue.poll();
            fullNotFull.signal();
        } finally {
            fullQueueLock.unlock();
        }
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
            freeNotEmpty.signal();
        } finally {
            freeQueueLock.unlock();
        }
    }

    public int getResourceCount() {
        return resourceCount;
    }

    private void initResources(int resourceCount) {
        while (resourceCount > 0) {
            freeQueue.add(createResource());
            resourceCount--;
        }
    }

    private Resource<T> createResource() {
        final Resource<T> resource = new Resource<T>();
        resource.setState(ResourceState.FREE);
        return resource;
    }

}
