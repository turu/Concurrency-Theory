package pl.agh.edu.tw.lab1;

/**
 * Author: Piotr Turek
 */
public class BinarySemaphore {
    private boolean isOpen = true;
    private int waitCount = 0;

    public BinarySemaphore(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void V() {
        synchronized (this) {
            if (waitCount > 0) {
                this.notify();
            }
            isOpen = true;
        }
    }

    public void P() throws InterruptedException {
        synchronized (this) {
            if (isOpen) {
                isOpen = false;
            } else {
                waitCount++;
                while (!isOpen) {
                    this.wait();
                }
                waitCount--;
                isOpen = false;
            }
        }
    }
}
