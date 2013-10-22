package pl.agh.edu.tw.lab2;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Author: Piotr Turek
 */
public class Buffer {
    private final Queue<Integer> buffer = new ArrayDeque<Integer>();
    private final int maxSize;

    public Buffer(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    public boolean isFull() {
        return buffer.size() == maxSize;
    }

    public Integer poll() {
        return buffer.poll();
    }

    public void add(Integer v) {
        if (isFull()) {
            throw new IllegalStateException("pl.agh.edu.tw.lab2.Buffer full!");
        }
        buffer.add(v);
    }
}
