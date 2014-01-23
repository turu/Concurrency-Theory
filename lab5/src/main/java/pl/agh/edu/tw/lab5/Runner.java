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

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private final Random random = new Random();
    private final AsynchronousResourceBuffer<Integer> buffer;
    private final List<Producer> producers = new LinkedList<Producer>();
    private final List<Consumer> consumers = new LinkedList<Consumer>();
    private final int playTimeInSec;
    private final int numOfProducers;
    private final int numOfConsumers;
    private int productionLimit;
    private int consumeLimit;

    public Runner(int maxSize, int playTimeInSec, int numOfProducers, int numOfConsumers, int productionLimit, int consumeLimit) {
        this.numOfProducers = numOfProducers;
        this.numOfConsumers = numOfConsumers;
        this.productionLimit = productionLimit;
        this.consumeLimit = consumeLimit;
        this.buffer = new AsynchronousResourceBuffer<Integer>(maxSize);
        this.playTimeInSec = playTimeInSec;
    }

    public void play() {
        try {
            submitTasks();
            TimeUnit.SECONDS.sleep(playTimeInSec);
            executorService.shutdown();
            LOG.error("DUPA: {}", executorService.awaitTermination(30, TimeUnit.SECONDS));
            executorService = Executors.newCachedThreadPool();
        } catch (InterruptedException e) {
            LOG.info("NumberWorkshop interrupted");
        }
    }

    private void submitTasks() {
        submitProducers();
        submitConsumers();
    }

    private void submitProducers() {
        for (int i = 0; i < numOfProducers; i++) {
            final Producer producer = new Producer(buffer, random.nextInt(1), random.nextInt(1), productionLimit);
            producers.add(producer);
            executorService.submit(producer);
        }
    }

    private void submitConsumers() {
        for (int i = 0; i < numOfConsumers; i++) {
            final Consumer consumer = new Consumer(buffer, random.nextInt(1), random.nextInt(1), consumeLimit);
            consumers.add(consumer);
            executorService.submit(consumer);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int bufferSize = 50, prodCount = 10, consCount = 10;
        final int productionLimit = 100;
        final int consumeLimit = 100;
        while (prodCount < 400) {
            LOG.info("Running {} elements, {} producers, {} consumers variant", bufferSize, prodCount, consCount);
            long startTime = System.currentTimeMillis();
            new Runner(bufferSize, 0, prodCount, consCount, productionLimit, consumeLimit).play();
            long endTime = System.currentTimeMillis();
            LOG.error("<<< Producers: {}, Consumers: {}, Buffer: {}, TOTAL_TIME: {} >>>", prodCount, consCount, bufferSize,
                    endTime - startTime);
            prodCount += 10;
            consCount += 10;
        }
    }
}
