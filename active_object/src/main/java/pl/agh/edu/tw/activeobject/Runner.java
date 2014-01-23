package pl.agh.edu.tw.activeobject;

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
    private final ResourceBufferProxyFactory<Integer> bufferProxyFactory;
    private final List<Producer> producers = new LinkedList<Producer>();
    private final List<Consumer> consumers = new LinkedList<Consumer>();
    private final int playTimeInSec;
    private final int numOfProducers;
    private final int numOfConsumers;
    private final int simultaneousComputationsTime;
    private final int processedLimit;

    public Runner(int maxSize, int playTimeInSec, int numOfProducers, int numOfConsumers,
                  int simultaneousComputationsTime, int processedLimit) {
        this.numOfProducers = numOfProducers;
        this.numOfConsumers = numOfConsumers;
        this.playTimeInSec = playTimeInSec;
        this.simultaneousComputationsTime = simultaneousComputationsTime;
        this.processedLimit = processedLimit;
        bufferProxyFactory = new ResourceBufferProxyFactory<Integer>(maxSize);
    }

    public void play() {
        try {
            executorService = Executors.newFixedThreadPool(numOfProducers + numOfConsumers);
            final long startTime = System.currentTimeMillis();
            submitTasks();
            executorService.shutdown();
            executorService.awaitTermination(playTimeInSec, TimeUnit.SECONDS);
            final long endTime = System.currentTimeMillis();
            final double seconds = (endTime - startTime) / 1000.;
            LOG.error("{} {}",simultaneousComputationsTime, seconds);
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
            final ResourceBufferProxy<Integer> proxy = bufferProxyFactory.getProxy();
            final Producer producer = new Producer(proxy, 0, simultaneousComputationsTime, processedLimit);
            producers.add(producer);
            executorService.submit(producer);
        }
    }

    private void submitConsumers() {
        for (int i = 0; i < numOfConsumers; i++) {
            ResourceBufferProxy<Integer> proxy = bufferProxyFactory.getProxy();
            final Consumer consumer = new Consumer(proxy, 0, simultaneousComputationsTime, processedLimit);
            consumers.add(consumer);
            executorService.submit(consumer);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int bufferSize = 20, producerCount = 2, consumerCount = 2;
        int simultaneousComputationsTime = 0;
        int processedLimit = 200;
        LOG.info("Running {} elements, {} producers, {} consumers variant", bufferSize, producerCount, consumerCount);
        while (simultaneousComputationsTime < 300) {
            new Runner(bufferSize, 30, producerCount, consumerCount, simultaneousComputationsTime, processedLimit).play();
            simultaneousComputationsTime += 10;
        }
    }
}

