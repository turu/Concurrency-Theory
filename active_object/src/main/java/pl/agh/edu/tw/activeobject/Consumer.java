package pl.agh.edu.tw.activeobject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Author: Piotr Turek
 */
public class Consumer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);
    private static int ID = 1;
    private final ResourceBufferProxy<Integer> buffer;
    private final Random random = new Random();
    private final int simultaneousComputationsTime;
    private final int sleepTimeInMs;
    private final int id = ID++;

    public Consumer(ResourceBufferProxy<Integer> buffer, int sleepTimeInMs, int simultaneousComputationsTime) {
        this.buffer = buffer;
        this.sleepTimeInMs = sleepTimeInMs;
        this.simultaneousComputationsTime = simultaneousComputationsTime;
    }

    @Override
    public void run() {
        try {
            while(!Thread.currentThread().isInterrupted()) {
                doConsume();
                TimeUnit.MILLISECONDS.sleep(sleepTimeInMs);
            }
        } catch (InterruptedException ex) {
            LOG.info("Consumer {} has been interrupted", id);
        }
    }

    private void doConsume() throws InterruptedException {
        final IFuture<Collection<Integer>> result = buffer.consume(random.nextInt(10));
        TimeUnit.MILLISECONDS.sleep(simultaneousComputationsTime);
        long totalTime = 0;
        if (!result.isAvailable()) {
            LOG.info("Consumption still not ended after {} ms. Waiting for completion.", simultaneousComputationsTime);
            final long startTime = System.currentTimeMillis();
            result.get();
            final long endTime = System.currentTimeMillis();
            totalTime = endTime - startTime;
        }
        LOG.info("Consumer {} consumed resources: {}. Total wait time: {}, simultaneous computations took {} ms",
                id, result.get(), totalTime, simultaneousComputationsTime);
    }
}
