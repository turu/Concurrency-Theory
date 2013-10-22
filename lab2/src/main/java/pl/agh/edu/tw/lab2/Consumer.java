package pl.agh.edu.tw.lab2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Author: Piotr Turek
 */
public class Consumer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);
    private final Buffer buffer;
    private final int sleepTimeInMs;

    public Consumer(Buffer buffer, int sleepTimeInMs) {
        this.buffer = buffer;
        this.sleepTimeInMs = sleepTimeInMs;
    }

    @Override
    public void run() {
        try {
            while(!Thread.currentThread().isInterrupted()) {
                synchronized (buffer) {
                    while (buffer.isEmpty()) {
                        buffer.wait();
                    }
                    doConsume();
                    buffer.notify();
                    TimeUnit.MILLISECONDS.sleep(sleepTimeInMs);
                }
            }
        } catch (InterruptedException ex) {
            LOG.info("pl.agh.edu.tw.lab2.Consumer has been interrupted");
        }
    }

    private void doConsume() {
        LOG.info("pl.agh.edu.tw.lab2.Consumer consumed {}", buffer.poll());
    }

}
