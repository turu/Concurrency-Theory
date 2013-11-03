package pl.agh.edu.tw.lab1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Author: Piotr Turek
 */
public class SProcess implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(SProcess.class);

    private final BinarySemaphore useNumber;
    private final Semaphore numberFull;
    private final Semaphore aggregateEmpty;
    private final Semaphore numberEmpty;
    private final BinarySemaphore aggregateFull;

    private final Queue<Integer> aggregateBuffer;
    private final Queue<Integer> numberBuffer;

    private final int aggregateSize;

    private final long sleepTimeInMs;

    private final Queue<Integer> innerQueue = new ArrayDeque<>();
    private BinarySemaphore useAggregate;

    public SProcess(BinarySemaphore useNumber, Semaphore numberFull, Semaphore aggregateEmpty, Semaphore numberEmpty,
                    BinarySemaphore aggregateFull, BinarySemaphore useAggregate,
                    Queue<Integer> aggregateBuffer, Queue<Integer> numberBuffer,
                    int aggregateSize, long sleepTimeInMs) {
        this.useNumber = useNumber;
        this.numberFull = numberFull;
        this.aggregateEmpty = aggregateEmpty;
        this.numberEmpty = numberEmpty;
        this.aggregateFull = aggregateFull;
        this.aggregateBuffer = aggregateBuffer;
        this.numberBuffer = numberBuffer;
        this.aggregateSize = aggregateSize;
        this.sleepTimeInMs = sleepTimeInMs;
        this.useAggregate = useAggregate;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                doRun();
            }
        } catch (InterruptedException ex) {
            LOG.info("S process has been interrupted");
        }
    }

    private void doRun() throws InterruptedException {
        numberFull.P();
//        numberFull.P();
        LOG.info("S process acquired numberFull");
        useNumber.P();
        LOG.info("S process acquired useNumber");
        innerQueue.add(numberBuffer.poll());
        innerQueue.add(numberBuffer.poll());
        useNumber.V();
        LOG.info("S process signalled useNumber");
        numberEmpty.V();
        numberEmpty.V();
        LOG.info("S process signalled numberEmpty");
        produceToAggregateBuffer();
        TimeUnit.MILLISECONDS.sleep(sleepTimeInMs);
    }

    private void produceToAggregateBuffer() throws InterruptedException {
        final Integer a = innerQueue.poll();
        final Integer b = innerQueue.poll();
        LOG.info("S process took two numbers from the top of innerQueue: {}, {}", a, b);
        aggregateEmpty.P();
        LOG.info("S process acquired aggregateEmpty");
        aggregateBuffer.add(a + b);
        LOG.info("S process added {} to the aggregate buffer", a+b);
        if (aggregateBuffer.size() == aggregateSize) {
            aggregateFull.V();
            LOG.info("Aggregate buffer full so S process signaled aggregateFull");
        }
    }
}
