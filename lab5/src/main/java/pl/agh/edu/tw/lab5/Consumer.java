package pl.agh.edu.tw.lab5;

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
    private final AsynchronousResourceBuffer<Integer> buffer;
    private final Random random = new Random();
    private final int sleepTimeInMs;
    private final int consTimeInMs;
    private final int id = ID++;

    public Consumer(AsynchronousResourceBuffer<Integer> buffer, int sleepTimeInMs, int consTimeInMs) {
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
        final Collection<Resource<Integer>> resources = buffer.consumeBegin(random.nextInt(10));
        LOG.info("Consumer {} retrieved resources {} to consume", id, resources);
        TimeUnit.MILLISECONDS.sleep(consTimeInMs);
        buffer.consumeEnd(resources);
        LOG.info("Consumer {} ended consumption of resources {}", id, resources);
    }
}
