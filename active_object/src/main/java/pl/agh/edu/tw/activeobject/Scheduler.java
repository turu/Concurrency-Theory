package pl.agh.edu.tw.activeobject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.java2d.Surface;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Author: Piotr Turek
 */
public class Scheduler<T> implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Scheduler.class);

    private final BlockingDeque<ProduceMethodRequest<T>> produceQueue =
            new LinkedBlockingDeque<ProduceMethodRequest<T>>();
    private final BlockingDeque<ConsumeMethodRequest<T>> consumeQueue =
            new LinkedBlockingDeque<ConsumeMethodRequest<T>>();

    private final Object lock = new Object();

    @SuppressWarnings("unchecked")
    public void schedule(IMethodRequest methodRequest) {
        if (methodRequest instanceof ProduceMethodRequest) {
            produceQueue.add((ProduceMethodRequest)methodRequest);
        } else {
            consumeQueue.add((ConsumeMethodRequest)methodRequest);
        }
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            synchronized (lock) {
                while (areBothEmpty()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        LOG.info("Scheduler interrupted");
                    }
                }
                tryDispatch();
            }
        }
    }

    private boolean areBothEmpty() {
        return produceQueue.isEmpty() && consumeQueue.isEmpty();
    }

    private void tryDispatch() {
        ArrayList<IMethodRequest> heads = getHeads();
        sortHeads(heads);
        IMethodRequest toExecute = getRequestToExecute(heads);
        doDispatch(heads, toExecute);
    }

    private void doDispatch(ArrayList<IMethodRequest> heads, IMethodRequest toExecute) {
        if (deadlockPresent(heads, toExecute)) {
            throw new IllegalStateException("Deadlock!");
        } else if (toExecute != null) {
            pollRequestQueue(toExecute);
            toExecute.execute();
        }
    }

    private void pollRequestQueue(IMethodRequest toExecute) {
        if (toExecute instanceof ProduceMethodRequest) {
            produceQueue.poll();
        } else {
            consumeQueue.poll();
        }
    }

    private boolean deadlockPresent(ArrayList<IMethodRequest> heads, IMethodRequest toExecute) {
        return toExecute == null && heads.size() > 1;
    }

    private IMethodRequest getRequestToExecute(ArrayList<IMethodRequest> heads) {
        IMethodRequest toExecute = null;
        for (IMethodRequest head : heads) {
            if (head.guard()) {
                toExecute = head;
                break;
            }
        }
        return toExecute;
    }

    private void sortHeads(ArrayList<IMethodRequest> heads) {
        Collections.sort(heads, new Comparator<IMethodRequest>() {
            @Override
            public int compare(IMethodRequest o1, IMethodRequest o2) {
                return o1.getPriority() - o2.getPriority() < 0 ? 1 : -1;
            }
        });
    }

    private ArrayList<IMethodRequest> getHeads() {
        ProduceMethodRequest<T> headProduce = produceQueue.peekFirst();
        ConsumeMethodRequest<T> headConsume = consumeQueue.peekFirst();
        ArrayList<IMethodRequest> heads = new ArrayList<IMethodRequest>();
        if (headConsume != null) {
            heads.add(headConsume);
        }
        if (headProduce != null) {
            heads.add(headProduce);
        }
        return heads;
    }
}
