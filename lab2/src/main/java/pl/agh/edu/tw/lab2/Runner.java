package pl.agh.edu.tw.lab2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: Piotr Turek
 */
public class Runner {
    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    private final Random random = new Random();
    private final Buffer buffer;
    private final List<Producer> producers = new LinkedList<Producer>();
    private final List<Consumer> consumers = new LinkedList<Consumer>();
    private final int playTimeInSec;
    private final int numOfProducers;
    private final int numOfConsumers;

    public Runner(int maxSize, int playTimeInSec, int numOfProducers, int numOfConsumers) {
        this.numOfProducers = numOfProducers;
        this.numOfConsumers = numOfConsumers;
        this.buffer = new Buffer(maxSize);
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
        submitConsumers();
        submitProducers();
    }

    private void submitProducers() {
        for (int i = 0; i < numOfProducers; i++) {
            final Producer producer = new Producer(buffer, random.nextInt(1000));
            producers.add(producer);
            executorService.submit(producer);
        }
    }

    private void submitConsumers() {
        for (int i = 0; i < numOfConsumers; i++) {
            final Consumer consumer = new Consumer(buffer, random.nextInt(1000));
            consumers.add(consumer);
            executorService.submit(consumer);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int n = 10, m = 20, l = 5;
        LOG.info("Running single element, single producer, single consumer variant...");
        new Runner(1, 30, 1, 1).play();
        TimeUnit.SECONDS.sleep(5);
        LOG.info("Running {} elements, single producer, single consumer variant", n);
        new Runner(n, 30, 1, 1).play();
        TimeUnit.SECONDS.sleep(5);
        LOG.info("Running single element, {} producers, {} consumers variant", m, l);
        new Runner(1, 30, m, l).play();
        TimeUnit.SECONDS.sleep(5);
        LOG.info("Running {} elements, {} producers, {} consumers variant", n, m, l);
        new Runner(n, 30, m, l).play();
    }
}
