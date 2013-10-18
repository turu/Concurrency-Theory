package pl.agh.edu.tw.lab1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Author: Piotr Turek
 */
public class PProcess implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(PProcess.class);
    private static volatile int pProcessCount = 1;
    private static volatile int totalProducedSum = 0;

    private final BinarySemaphore useNumber;
    private final Semaphore numberEmpty;
    private final Semaphore numberFull;

    private final Queue<Integer> numberBuffer;

    private final int sleepTimeInMs;
    private final Random random = new Random();
    private final long uid = pProcessCount++;

    public PProcess(BinarySemaphore useNumber, Semaphore numberEmpty, Semaphore numberFull, Queue<Integer> numberBuffer,
                    int sleepTimeInMs) {
        this.useNumber = useNumber;
        this.numberEmpty = numberEmpty;
        this.numberFull = numberFull;
        this.numberBuffer = numberBuffer;
        this.sleepTimeInMs = sleepTimeInMs;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                numberEmpty.P();
                LOG.info("P process {} acquired numberEmpty", uid);
                useNumber.P();
                LOG.info("P process {} acquired useNumber", uid);
                final int toAdd = random.nextInt(100);
                numberBuffer.add(toAdd);
                totalProducedSum += toAdd;
                LOG.info("P process {} added {} to the number buffer", uid, toAdd);
                useNumber.V();
                LOG.info("P process {} signalled useNumber", uid);
                numberFull.V();
                LOG.info("P process {} signalled numberFull", uid);
                TimeUnit.MILLISECONDS.sleep(sleepTimeInMs);
            }
        } catch (InterruptedException ex) {
            LOG.info("P process {} has been interrupted", uid);
        }
    }

    public static int getTotalProducedSum() {
        return totalProducedSum;
    }

}
