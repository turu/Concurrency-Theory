package pl.agh.edu.tw.lab4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Author: Piotr Turek
 */
public class SynchronousConsumer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(SynchronousConsumer.class);
    private static int ID = 1;
    private final Buffer<Integer> buffer;
    private final Random random = new Random();
    private final int sleepTimeInMs;
    private final int consTimeInMs;
    private final int id = ID++;

    public SynchronousConsumer(Buffer<Integer> buffer, int sleepTimeInMs, int consTimeInMs) {
        this.buffer = buffer;
        this.sleepTimeInMs = sleepTimeInMs;
        this.consTimeInMs = consTimeInMs;
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
        final List<Integer> resource = buffer.consume(random.nextInt(10) + 1);
        LOG.info("Consumer {} retrieved resource {} to consume", id, resource);
        TimeUnit.MILLISECONDS.sleep(consTimeInMs);
    }
}
