package pl.agh.edu.tw.lab1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Author: Piotr Turek
 */
public class KProcess implements Runnable {
    private final static Logger LOG = LoggerFactory.getLogger(KProcess.class);
    private static volatile int totalConsumedSum = 0;

    private final BinarySemaphore aggregateFull;
    private final Semaphore aggregateEmpty;
    private final long sleepTimeInMs;
    private final Queue<Integer> aggregateBuffer;
    private BinarySemaphore useAggregate;

    public KProcess(BinarySemaphore aggregateFull, Semaphore aggregateEmpty, BinarySemaphore useAggregate,
                    long sleepTimeInMs, Queue<Integer> aggregateBuffer) {
        this.aggregateFull = aggregateFull;
        this.aggregateEmpty = aggregateEmpty;
        this.sleepTimeInMs = sleepTimeInMs;
        this.aggregateBuffer = aggregateBuffer;
        this.useAggregate = useAggregate;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                aggregateFull.P();
                LOG.info("Process K acquired aggregateFull");
                useAggregate.P();
                LOG.info("Process K acquired useAggregate");
                displayAndEmpty();
                useAggregate.V();
                LOG.info("Process K signalled useAggregate");
                signalAggregateBuffer();
                TimeUnit.MILLISECONDS.sleep(sleepTimeInMs);
            }
        } catch (InterruptedException e) {
            LOG.info("K process has been interrupted");
        }
    }

    public static int getTotalConsumedSum() {
        return totalConsumedSum;
    }

    private void signalAggregateBuffer() {
        LOG.info("Signaling the aggregateEmpty semaphore");
        for (int i = 0; i < aggregateBuffer.size(); i++) {
            aggregateEmpty.V();
            LOG.info("The aggregateFull semaphore signalled");
        }
        aggregateBuffer.clear();
    }

    private void displayAndEmpty() {
        LOG.info("Emptying aggregate buffer: {}", aggregateBuffer);
        int sum = 0;
        for (Integer v : aggregateBuffer) {
            sum += v;
        }
        totalConsumedSum += sum;
        LOG.info("Aggregate buffer emptied, sum: {}", sum);
    }
}
