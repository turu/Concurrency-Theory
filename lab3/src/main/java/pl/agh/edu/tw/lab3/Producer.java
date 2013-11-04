package pl.agh.edu.tw.lab3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Author: Piotr Turek
 */
public class Producer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Producer.class);
    private static int ID = 1;
    private final AsynchronousResourceBuffer<Integer> buffer;
    private final Random random = new Random();
    private final int sleepTimeInMs;
    private final int prodTimeInMs;
    private final int id = ID++;

    public Producer(AsynchronousResourceBuffer<Integer> buffer, int sleepTimeInMs, int prodTimeInMs) {
        this.buffer = buffer;
        this.sleepTimeInMs = sleepTimeInMs;
        this.prodTimeInMs = prodTimeInMs;
    }

    @Override
    public void run() {
        try {
            while(!Thread.currentThread().isInterrupted()) {
                doProduce();
                TimeUnit.MILLISECONDS.sleep(sleepTimeInMs);
            }
        } catch (InterruptedException ex) {
            LOG.info("Producer {} has been interrupted", id);
        }
    }

    private void doProduce() throws InterruptedException {
        final Resource<Integer> resource = buffer.produceBegin();
        LOG.info("Producer {} acquired resource to produce", id);
        TimeUnit.MILLISECONDS.sleep(prodTimeInMs);
        resource.setValue(random.nextInt(1000));
        buffer.produceEnd(resource);
        LOG.info("Producer {} produced resource {}", id, resource);
    }
}
