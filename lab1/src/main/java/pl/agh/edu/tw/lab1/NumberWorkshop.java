package pl.agh.edu.tw.lab1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: Piotr Turek
 */
public class NumberWorkshop {
    private static final Logger LOG = LoggerFactory.getLogger(NumberWorkshop.class);

    private final Random random = new Random();
    private final ExecutorService executorService = new ScheduledThreadPoolExecutor(10);
    private final Queue<Integer> numberBuffer = new ArrayDeque<>();
    private final Queue<Integer> aggregateBuffer = new ArrayDeque<>();
    private final BinarySemaphore useAggregate = new BinarySemaphore(true);
    private final BinarySemaphore aggregateFull = new BinarySemaphore(false);
    private final Semaphore aggregateEmpty;
    private final Semaphore numberFull;
    private final BinarySemaphore useNumber = new BinarySemaphore(true);
    private final Semaphore numberEmpty;
    private final List<PProcess> producers = new ArrayList<>();
    private final SProcess sProcess;
    private final KProcess kProcess;

    public NumberWorkshop(int n, int m, int pCount) {
        aggregateEmpty = new Semaphore(n);
        numberFull = new Semaphore(0);
        numberEmpty = new Semaphore(m);

        for (int i = 0; i < pCount; i++) {
            producers.add(new PProcess(useNumber, numberEmpty, numberFull, numberBuffer, random.nextInt(1000)));
        }
        sProcess = new SProcess(useNumber, numberFull, aggregateEmpty, numberEmpty, aggregateFull, useAggregate,
                aggregateBuffer, numberBuffer, n,
                random.nextInt(1000));
        kProcess = new KProcess(aggregateFull, aggregateEmpty, useAggregate, random.nextInt(1000), aggregateBuffer);
    }

    public void play(int playTimeInSec) {
        try {
            executorService.submit(kProcess);
            executorService.submit(sProcess);
            for (PProcess p : producers) {
                executorService.submit(p);
                TimeUnit.MILLISECONDS.sleep(random.nextInt(1000));
            }
            TimeUnit.SECONDS.sleep(playTimeInSec);
        } catch (InterruptedException e) {
            LOG.info("NumberWorkshop interrupted");
        }
        executorService.shutdownNow();
        LOG.info("Total sum of produced numbers: {}", PProcess.getTotalProducedSum());
        LOG.info("Total sum of consumed numbers: {}", KProcess.getTotalConsumedSum());
        final int numberSum = sumOfNumbersInBuffer(numberBuffer);
        LOG.info("Total sum of numbers left in number buffer: {}", numberSum);
        final int aggregateSum = sumOfNumbersInBuffer(aggregateBuffer);
        LOG.info("Total sum of numbers left in aggregate buffer: {}", aggregateSum);
        LOG.info("Total sum of numbers left in buffers: {}", aggregateSum + numberSum);
    }

    private int sumOfNumbersInBuffer(Queue<Integer> buffer) {
        int sum = 0;
        for (Integer v : buffer) {
            sum += v;
        }
        return sum;
    }

    public static void main(String[] args) {
        int n = 5;
        int m = 10;
        int pCount = 10;

        new NumberWorkshop(n, m, pCount).play(30);
    }
}
