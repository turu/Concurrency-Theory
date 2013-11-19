package pl.agh.edu.tw.lab5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Author: Piotr Turek
 */
public class Runner {
    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Random random = new Random();
    private final Buffer<Integer> buffer;
    private final List<SynchronousProducer> producers = new LinkedList<SynchronousProducer>();
    private final List<SynchronousConsumer> consumers = new LinkedList<SynchronousConsumer>();
    private final int playTimeInSec;
    private final int numOfProducers;
    private final int numOfConsumers;

    public Runner(int maxSize, int playTimeInSec, int numOfProducers, int numOfConsumers) {
        this.numOfProducers = numOfProducers;
        this.numOfConsumers = numOfConsumers;
        this.buffer = new Buffer<Integer>(maxSize);
        this.playTimeInSec = playTimeInSec;
    }

    public void play() {
        try {
            submitTasks();
            TimeUnit.SECONDS.sleep(playTimeInSec);
        } catch (InterruptedException e) {
            LOG.info("NumberWorkshop interrupted");
        }
        executorService.shutdownNow();
    }

    private void submitTasks() {
        submitProducers();
        submitConsumers();
    }

    private void submitProducers() {
        for (int i = 0; i < numOfProducers; i++) {
            final SynchronousProducer producer = new SynchronousProducer(buffer, random.nextInt(1000), random.nextInt(1000));
            producers.add(producer);
            executorService.submit(producer);
        }
    }

    private void submitConsumers() {
        for (int i = 0; i < numOfConsumers; i++) {
            final SynchronousConsumer consumer = new SynchronousConsumer(buffer, random.nextInt(1000), random.nextInt(1000));
            consumers.add(consumer);
            executorService.submit(consumer);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int n = 100, m = 20, l = 5;
        LOG.info("Running {} elements, {} producers, {} consumers variant", n, m, l);
        new Runner(n, 30, m, l).play();
    }
}
