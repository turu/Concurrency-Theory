package pl.agh.edu.tw.activeobject;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Author: Piotr Turek
 */
public class Scheduler<T> implements Runnable {

    private final BlockingDeque<ProduceMethodRequest<T>> produceQueue =
            new LinkedBlockingDeque<ProduceMethodRequest<T>>();
    private final BlockingDeque<ConsumeMethodRequest<T>> consumeQueue =
            new LinkedBlockingDeque<ConsumeMethodRequest<T>>();

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            tryDispatch();
        }
    }

    private void tryDispatch() {
        ProduceMethodRequest<T> headProduce = produceQueue.peekFirst();
        ConsumeMethodRequest<T> headConsume = consumeQueue.peekFirst();
        ArrayList<IMethodRequest> heads = new ArrayList<IMethodRequest>();
        if (headConsume != null) {
            heads.add(headConsume);
        }
        if (headProduce != null) {
            heads.add(headProduce);
        }
        Collections.sort(heads, new Comparator<IMethodRequest>() {
            @Override
            public int compare(IMethodRequest o1, IMethodRequest o2) {
                return o1.getPriority() - o2.getPriority() < 0 ? 1 : -1;
            }
        });
        IMethodRequest toExecute = null;
        for (IMethodRequest head : heads) {
            if (head.guard()) {
                toExecute = head;
                break;
            }
        }
        if (toExecute == null && heads.size() > 1) {
            throw new IllegalStateException("Deadlock!");
        } else if (toExecute != null) {
            if (toExecute instanceof ProduceMethodRequest) {
                produceQueue.poll();
            } else {
                consumeQueue.poll();
            }
            toExecute.execute();
        }

    }

    public void schedule(IMethodRequest methodRequest) {
        if (methodRequest instanceof ProduceMethodRequest) {
            produceQueue.add((ProduceMethodRequest)methodRequest);
        } else {
            consumeQueue.add((ConsumeMethodRequest)methodRequest);
        }
    }
}
