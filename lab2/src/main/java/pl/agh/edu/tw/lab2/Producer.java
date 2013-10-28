package pl.agh.edu.tw.lab2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Author: Piotr Turek
 */
public class Producer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);
    private static int ID = 1;
    private final Buffer buffer;
    private final Random random = new Random();
    private final int sleepTimeInMs;
    private final int id = ID++;

    public Producer(Buffer buffer, int sleepTimeInMs) {
        this.buffer = buffer;
        this.sleepTimeInMs = sleepTimeInMs;
    }

    @Override
    public void run() {
        try {
            while(!Thread.currentThread().isInterrupted()) {
                synchronized (buffer) {
                    while (buffer.isFull()) {
                        LOG.info("Producer {} waiting on lock", id);
                        buffer.wait();
                    }
                    doProduce();
                    buffer.notify();
                }
                TimeUnit.MILLISECONDS.sleep(sleepTimeInMs);
            }
        } catch (InterruptedException ex) {
            LOG.info("Producer has been interrupted");
        }
    }

    private void doProduce() {
        final int v = random.nextInt(1000);
        buffer.add(v);
        LOG.info("Producer {} produced {}", id, v);
    }
}
