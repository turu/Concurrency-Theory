package pl.agh.edu.tw.lab1;

/**
 * Author: Piotr Turek
 */
public class Semaphore {
    private int value;
    private int waitCount = 0;

    public Semaphore(int value) {
        this.value = value;
    }

    public void V() {
        synchronized (this) {
            if (waitCount > 0) {
                value++;
                this.notify();
            } else {
                value++;
            }
        }
    }

    public void P() throws InterruptedException {
        synchronized (this) {
            if (value > 0) {
                value--;
            } else {
                waitCount++;
                while (value == 0) {
                    this.wait();
                }
                waitCount--;
                value--;
            }
        }
    }
}
