package pl.agh.edu.tw.lab5;

import com.sun.org.apache.xml.internal.serialize.ElementState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: Piotr Turek
 */
public class AsynchronousResourceBuffer<T> {
    private final static Logger LOG = LoggerFactory.getLogger(AsynchronousResourceBuffer.class);

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition firstForFull = lock.newCondition();
    private final Condition firstForEmpty = lock.newCondition();
    private final Condition restForFull = lock.newCondition();
    private final Condition restForEmpty = lock.newCondition();

    private final Queue<Resource<T>> emptyResources;
    private final Queue<Resource<T>> fullResources;
    private final int maxSize;

    public AsynchronousResourceBuffer(int maxSize) {
        this.emptyResources = new LinkedList<Resource<T>>();
        this.fullResources = new LinkedList<Resource<T>>();
        this.maxSize = maxSize;
        for (int i = 0; i < this.maxSize; i++) {
            emptyResources.add(new Resource<T>());
        }
    }

    public Collection<Resource<T>> produceBegin(int chunkSize) throws InterruptedException {
        Collection<Resource<T>> ret = new LinkedList<Resource<T>>();
        lock.lock();
        try {
            while (lock.hasWaiters(firstForEmpty)) {
                restForEmpty.await();
            }

            while (emptyResources.size() < chunkSize) {
                firstForEmpty.await();
            }

            doProduce(chunkSize, ret);

            restForEmpty.signal();
        } finally {
            lock.unlock();
        }

        return ret;
    }

    private void doProduce(int chunkSize, Collection<Resource<T>> ret) {
        while (chunkSize-- > 0) {
            Resource<T> resource = emptyResources.poll();
            ret.add(resource);
            resource.setState(ResourceState.IN_PRODUCTION);
        }
    }

    public void produceEnd(Collection<Resource<T>> chunk) {
        lock.lock();
        try {
            for (Resource<T> res : chunk) {
                fullResources.add(res);
                res.setState(ResourceState.FULL);
            }
            firstForFull.signal();
        } finally {
            lock.unlock();
        }
    }


    public Collection<Resource<T>> consumeBegin(int chunkSize) throws InterruptedException {
        Collection<Resource<T>> ret = new LinkedList<Resource<T>>();
        lock.lock();
        try {
            while (lock.hasWaiters(firstForFull)) {
                restForFull.await();
            }

            while (fullResources.size() < chunkSize) {
                firstForFull.await();
            }

            doConsume(chunkSize, ret);

            restForFull.signal();
        } finally {
            lock.unlock();
        }
        return ret;
    }

    private void doConsume(int chunkSize, Collection<Resource<T>> ret) {
        while (chunkSize-- > 0) {
            Resource<T> res = fullResources.poll();
            ret.add(res);
            res.setState(ResourceState.IN_CONSUMPTION);
        }
    }

    public void consumeEnd(Collection<Resource<T>> chunk) {
        lock.lock();
        try {
            for (Resource<T> res : chunk) {
                emptyResources.add(res);
                res.setState(ResourceState.FREE);
            }
            firstForEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

}
