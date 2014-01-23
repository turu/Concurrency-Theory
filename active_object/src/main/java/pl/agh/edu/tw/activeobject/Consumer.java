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
public class Consumer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);
    private static int ID = 1;
    private final ResourceBufferProxy<Integer> buffer;
    private final Random random = new Random();
    private final int simultaneousComputationsTime;
    private final int sleepTimeInMs;
    private final int id = ID++;
    private final int consLimit;

    public Consumer(ResourceBufferProxy<Integer> buffer, int sleepTimeInMs, int simultaneousComputationsTime,
                    int consLimit) {
        this.buffer = buffer;
        this.sleepTimeInMs = sleepTimeInMs;
        this.simultaneousComputationsTime = simultaneousComputationsTime;
        this.consLimit = consLimit;
    }

    @Override
    public void run() {
        try {
            int consCounter = 0;
            while(!Thread.currentThread().isInterrupted() && consCounter < consLimit) {
                consCounter += doConsume(consCounter);
                TimeUnit.MILLISECONDS.sleep(sleepTimeInMs);
            }
            LOG.info("Consumer {} ended processing", id);
        } catch (InterruptedException ex) {
            LOG.info("Consumer {} has been interrupted", id);
        }
    }

    private int doConsume(int consCounter) throws InterruptedException {
        final IFuture<Collection<Integer>> result = buffer.consume(computeChunkSize(consCounter));
        TimeUnit.MILLISECONDS.sleep(simultaneousComputationsTime);
        long totalTime = 0;
        final Collection<Integer> chunk = new ArrayList<Integer>();
        if (!result.isAvailable()) {
            LOG.info("Consumption still not ended after {} ms. Waiting for completion.", simultaneousComputationsTime);
            final long startTime = System.currentTimeMillis();
            chunk.addAll(result.get());
            final long endTime = System.currentTimeMillis();
            totalTime = endTime - startTime;
        } else {
            chunk.addAll(result.get());
        }
        LOG.info("Consumer {} consumed resources: {}. Total wait time: {}, simultaneous computations took {} ms",
                id, chunk, totalTime, simultaneousComputationsTime);
        return chunk.size();
    }

    private int computeChunkSize(int consCounter) {
        return Math.min(random.nextInt(10), consLimit - consCounter);
    }
}
