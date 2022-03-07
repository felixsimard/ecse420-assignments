package ca.mcgill.ecse420.a2;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Filter {

    public static class FilterLock implements Lock {
        int n;
        int[] level;
        int [] victim;
        ArrayList<Integer> availableIds;
        ThreadLocal<Integer> myId = ThreadLocal.withInitial(() -> -1);

        public FilterLock(int n) {
            this.n = n;
            level = new int[n];
            victim = new int[n];
            availableIds = new ArrayList<>();
            for (int i=0; i<n; i++){
                availableIds.add(i);
            }
        }

        @Override
        public void lock() {
            if (myId.get() == -1) {
                Integer nextId = null;
                synchronized (availableIds) {
                    if (availableIds.size() != 0) {
                        nextId = availableIds.get(0);
                        availableIds.remove(nextId);
                    }
                }
                myId.set(nextId);
            }

            System.out.println("\nThread " + myId.get() + " locking...");

            int i = myId.get();
            boolean spin = true;

            for (int L = 1; L < n; L++) {
                level[i] = L;
                victim[L] = i;

                do {
                    spin = false;
                    for (int j=0; j < n; j++) {
                        if (j != i && level[j] >= L && victim[L] == i) {
                            spin = true;
                        }
                    }
                } while (spin);

            }
            System.out.println("\nThread " + myId.get() + " locked.");
        }

        @Override
        public void unlock() {
            System.out.println("\nThread " + myId.get() + " unlocking...");
            level[myId.get()] = 0;
            System.out.println("Thread " + myId.get() + " unlocked.");

        }

        @Override
        public void lockInterruptibly() throws InterruptedException {

        }

        @Override
        public boolean tryLock() {
            return false;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public Condition newCondition() {
            return null;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int n = 5;
        FilterLock filterLock = new FilterLock(n);

        Thread[] threads = new Thread[n];
        for (int i=0; i<n; i++) {
            threads[i] = new Thread(() -> {
                filterLock.lock();
                try {
                    Thread.sleep(new Random().nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                filterLock.unlock();
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
