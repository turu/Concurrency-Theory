package pl.agh.edu.tw.lab5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
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
    private final int productionLimit;

    public Producer(AsynchronousResourceBuffer<Integer> buffer, int sleepTimeInMs, int prodTimeInMs, int productionLimit) {
        this.buffer = buffer;
        this.sleepTimeInMs = sleepTimeInMs;
        this.prodTimeInMs = prodTimeInMs;
        this.productionLimit = productionLimit;
    }

    @Override
    public void run() {
        try {
            int prodCounter = 0;
            while(!Thread.currentThread().isInterrupted() && prodCounter < productionLimit) {
                prodCounter += doProduce();
                TimeUnit.MILLISECONDS.sleep(sleepTimeInMs);
            }
        } catch (InterruptedException ex) {
            LOG.info("Producer {} has been interrupted", id);
        }
    }

    private int doProduce() throws InterruptedException {
        final int chunkSize = random.nextInt(9) + 1;
        final Collection<Resource<Integer>> resources = buffer.produceBegin(chunkSize);
        LOG.info("Producer {} acquired resources to produce", id);
        TimeUnit.MILLISECONDS.sleep(prodTimeInMs);
        for (Resource<Integer> res : resources) {
            res.setValue(random.nextInt(1000));
        }
        buffer.produceEnd(resources);
        LOG.info("Producer {} produced resources {}", id, resources);
        return chunkSize;
    }
}
