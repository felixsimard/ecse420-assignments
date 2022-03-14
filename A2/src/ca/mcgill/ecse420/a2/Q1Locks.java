package ca.mcgill.ecse420.a2;

import java.util.Random;
import java.util.concurrent.locks.Lock;

public class Q1Locks {

    public static Lock getLock(boolean filterLock, int n) {
        return filterLock ? new FilterLock(n) : new BakeryLock(n);
    }

    public static void main(String[] args) throws InterruptedException {
        int n = 10;
        boolean filterLock = true;
        Lock lock = getLock(filterLock, n);

        Thread[] threads = new Thread[n];
        for (int i=0; i<n; i++) {
            threads[i] = new Thread(() -> {
                lock.lock();
                try {
                    Thread.sleep(new Random().nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.unlock();
            });
        }

        for (int i=0; i<n; i++) {
            threads[i].start();
        }

        for (int i=0; i<n; i++) {
            threads[i].join();
        }

    }
}
