package pl.agh.edu.tw.activeobject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Author: Piotr Turek
 */
public class Producer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Producer.class);

    private static int ID = 1;
    private final ResourceBufferProxy<Integer> bufferProxy;
    private final Random random = new Random();
    private final int sleepTimeInMs;
    private final int simultaneousComputationsTime;
    private final int id = ID++;

    public Producer(ResourceBufferProxy<Integer> bufferProxy, int sleepTimeInMs, int simultaneousComputationsTime) {
        this.bufferProxy = bufferProxy;
        this.sleepTimeInMs = sleepTimeInMs;
        this.simultaneousComputationsTime = simultaneousComputationsTime;
    }


    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                doProduce();
                TimeUnit.MILLISECONDS.sleep(sleepTimeInMs);
            }
        } catch (InterruptedException ex) {
            LOG.info("Producer {} has been interrupted", id);
        }

    }

    private void doProduce() throws InterruptedException {
        final int chunkSize = random.nextInt(10);
        final Collection<Integer> chunk = new ArrayList<Integer>();
        for (int i = 0; i < chunkSize; i++) {
            chunk.add(random.nextInt(1000));
        }
        final IFuture<Void> produce = bufferProxy.produce(chunk);
        LOG.info("Producer {} submitted for production", id);
        TimeUnit.MILLISECONDS.sleep(simultaneousComputationsTime);
        long totalTime = 0;
        if (!produce.isAvailable()) {
            LOG.info("Production still not ended after {} ms. Waiting for completion.", simultaneousComputationsTime);
            final long startTime = System.currentTimeMillis();
            produce.get();
            final long endTime = System.currentTimeMillis();
            totalTime = endTime - startTime;
        }
        LOG.info("Producer {} produced resources: {}. Total wait time: {}, simultaneous computations took {} ms",
                id, chunk, totalTime, simultaneousComputationsTime);
    }
}
