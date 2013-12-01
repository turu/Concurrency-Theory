package pl.agh.edu.tw.activeobject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Author: Piotr Turek
 */
public class ResourceBuffer<T> {
    public static final Logger LOG = LoggerFactory.getLogger(ResourceBuffer.class);

    private final Queue<T> fullResources;
    private final int maxSize;

    public ResourceBuffer(int maxSize) {
        this.fullResources = new LinkedList<T>();
        this.maxSize = maxSize;
    }


    public boolean canProduce(int chunkSize) {
        if (fullResources.size() + chunkSize <= maxSize) {
            return true;
        }
        return false;
    }

    public void produce(Collection<T> chunk) {
        if (!canProduce(chunk.size())) {
            throw new IllegalArgumentException();
        }
        fullResources.addAll(chunk);
    }

    public boolean canConsume(int chunkSize) {
        if (fullResources.size() >= chunkSize) {
            return true;
        }
        return false;
    }

    public Collection<T> consume(int chunkSize) {
        if (!canConsume(chunkSize)) {
            throw new IllegalArgumentException();
        }
        Collection<T> ret = new ArrayList<T>();
        while (chunkSize-- > 0) {
            ret.add(fullResources.poll());
        }
        return ret;
    }

    public int getMaxSize() {
        return maxSize;
    }

}
