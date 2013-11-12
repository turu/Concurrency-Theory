package pl.agh.edu.tw.lab4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
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
    private final Condition firstProducer = freeQueueLock.newCondition();
    private final Condition firstConsumer = fullQueueLock.newCondition();

    private final Queue<Resource<T>> freeQueue = new ArrayDeque<Resource<T>>();
    private final Queue<Resource<T>> fullQueue = new ArrayDeque<Resource<T>>();
    private final int resourceCount;
    private final int maxChunkSize;
    private volatile int freeQueueSize;
    private volatile int fullQueueSize;
    private boolean firstProducerWaits = false;
    private boolean firstConsumerWaits = false;

    public AsynchronousResourceBuffer(int resourceCount) {
        this.resourceCount = resourceCount;
        this.maxChunkSize = resourceCount / 2;
        initResources(resourceCount);
    }

    public List<Resource<T>> produceBegin(int chunkSize) throws InterruptedException {
        if (chunkSize > maxChunkSize) {
            throw new IllegalArgumentException();
        }
        Resource<T> resource = null;
        freeQueueLock.lock();
        try {
            while (freeQueue.isEmpty()) {
                freeNotEmpty.await();
            }
            resource = freeQueue.poll();
            freeQueueSize--;
            freeNotFull.signal();
        } finally {
            freeQueueLock.unlock();
        }
        resource.setState(ResourceState.IN_PRODUCTION);
        resource.setValue(null);
        return Arrays.asList(resource);
    }

    public void produceEnd(List<Resource<T>> resources) throws InterruptedException {

//        resource.setState(ResourceState.FULL);
        fullQueueLock.lock();
        try {
            while (fullQueue.size() == resourceCount - freeQueueSize) {
                fullNotFull.await();
            }
//            fullQueue.add(resource);
            fullQueueSize++;
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
            fullQueueSize--;
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
            while (freeQueue.size() == resourceCount - fullQueueSize) {
                freeNotFull.await();
            }
            freeQueue.add(resource);
            freeQueueSize++;
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
