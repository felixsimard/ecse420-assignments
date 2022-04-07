package ca.mcgill.ecse420.a3;

import java.util.ArrayList;
import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedQueue {

    public enum QueueAction {ENQUEUE, DEQUEUE}

    public static SplittableRandom random = new SplittableRandom();

    public static LockBasedQueue<Integer> lbq;
    public static LockFreeQueue<Integer> lfq;

    public static ArrayList<Thread> allThreads = new ArrayList<>();

    public static int NUM_THREADS = 4;

    /**
     * Lock-based, array-based queue
     */
    public static class LockBasedQueue<T> {
        ReentrantLock headLock;
        ReentrantLock tailLock;
        AtomicInteger size;
        AtomicInteger headIndex;
        AtomicInteger tailIndex;
        int capacity;
        T[] items;
        Condition notEmptyCondition, notFullCondition;

        public LockBasedQueue(int capacity) {
            this.capacity = capacity;
            this.items = (T[]) new Object[capacity];
            this.size = new AtomicInteger(0);
            this.headLock = new ReentrantLock();
            this.tailLock = new ReentrantLock();
            this.headIndex = new AtomicInteger(0);
            this.tailIndex = new AtomicInteger(0);
            this.notEmptyCondition = headLock.newCondition();
            this.notFullCondition = tailLock.newCondition();
        }

        /**
         * Enqueue
         */
        public void enqueue(Object x) throws InterruptedException {
            boolean wakeDequeuers = false;
            tailLock.lock();
            try {
                while (size.get() == capacity) notFullCondition.await();
                items[tailIndex.get()] = (T) x;
                if (tailIndex.incrementAndGet() == capacity) tailIndex.set(0);
                if (size.getAndIncrement() == 0) {
                    wakeDequeuers = true;
                }
            } finally {
                tailLock.unlock();
            }
            if (wakeDequeuers) {
                headLock.lock();
                try {
                    notEmptyCondition.signalAll();
                } finally {
                    headLock.unlock();
                }
            }
        }

        /**
         * Dequeue
         */
        public T dequeue() throws InterruptedException {
            Object x;
            boolean wakeEnqueuers = false;
            headLock.lock();
            try {
                while (size.get() == 0) notEmptyCondition.await();
                x = items[headIndex.get()];
                items[headIndex.get()] = null;
                if (headIndex.incrementAndGet() == capacity) {
                    headIndex.set(0);
                    //wakeEnqueuers = true;
                }
                if (size.getAndDecrement() == capacity) {
                    wakeEnqueuers = true;
                }
                //size.decrementAndGet();
            } finally {
                headLock.unlock();
            }
            if (wakeEnqueuers) {
                tailLock.lock();
                try {
                    notFullCondition.signalAll();
                } finally {
                    tailLock.unlock();
                }
            }
            return (T) x;
        }

        public void printQueue() {
            System.out.println("------------------");
            for (int i = 0; i < capacity; i++) {
                System.out.print(String.format("%s | ", items[i]));
            }
            System.out.println("");
            System.out.println(String.format("Head index: %s", headIndex));
            System.out.println(String.format("Tail index: %s", tailIndex));
        }


    }

    /**
     * Lock-free array based queue
     */
    public static class LockFreeQueue<T> {
        AtomicInteger headIndex;
        AtomicInteger tailIndex;
        AtomicInteger size;
        int capacity;
        AtomicReferenceArray<T> items;

        public LockFreeQueue(int capacity) {
            this.capacity = capacity;
            this.items = new AtomicReferenceArray<T>(capacity);
            this.size = new AtomicInteger(0);
            this.headIndex = new AtomicInteger(0);
            this.tailIndex = new AtomicInteger(0);
        }

        /**
         * Enqueue
         */
        public void enqueue(Object x) {
            while (true) {
                Object last = items.get(tailIndex.get());
                if (last == items.get(tailIndex.get())) {
                    if (items.compareAndSet(tailIndex.get(), (T) null, (T) x)) {
                        size.incrementAndGet();
                        if (tailIndex.incrementAndGet() == capacity) tailIndex.set(0);
                        return;
                    }
                }
            }
        }

        /**
         * Dequeue
         */
        public T dequeue() {
            while (true) {
                Object first = items.get(headIndex.get());
                if (first == items.get(headIndex.get())) {
                    if (items.compareAndSet(headIndex.get(), (T) first, null)) {
                        size.decrementAndGet();
                        if (headIndex.incrementAndGet() == capacity) headIndex.set(0);
                        return (T) first;
                    }
                }
            }
        }

        public void printQueue() {
            System.out.println("------------------");
            for (int i = 0; i < capacity; i++) {
                System.out.print(String.format("%s | ", items.get(i)));
            }
            System.out.println("");
            System.out.println(String.format("Head index: %s", headIndex));
            System.out.println(String.format("Tail index: %s", tailIndex));
        }

    }

    /**
     * QueueWorker
     */
    public static class QueueWorker implements Runnable {
        int id;
        boolean isLockBased;
        int numActionsToPerform = 5;

        public QueueWorker(int id, boolean isLockBased) {
            this.id = id;
            this.isLockBased = isLockBased;
        }

        @Override
        public void run() {
            while (numActionsToPerform > 0) {
                //QueueAction qa = pickAction();
                QueueAction qa = QueueAction.DEQUEUE;
                if (this.id % 2 == 0) {
                    qa = QueueAction.ENQUEUE;
                }
                try {
                    performAction(qa, isLockBased);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                numActionsToPerform--;
            }
            return;
        }

        public void performAction(QueueAction qa, boolean isLockBased) throws InterruptedException {
            int obj = random.nextInt(0, 100);
            if (isLockBased) {
                System.out.println(String.format("Thread #%s %s on LOCK-BASED queue.", id, qa.toString()));
                switch (qa) {
                    case ENQUEUE:
                        lbq.enqueue(obj);
                        break;
                    case DEQUEUE:
                        lbq.dequeue();
                        break;
                }
            } else {
                System.out.println(String.format("Thread #%s %s on LOCK-FREE queue.", id, qa.toString()));
                switch (qa) {
                    case ENQUEUE:
                        lfq.enqueue(obj);
                        break;
                    case DEQUEUE:
                        lfq.dequeue();
                        break;
                }
            }
        }

    }

    /**
     * Pick the enqueue or dequeue action (50-50).
     */
    public static QueueAction pickAction() {
        int randInt = random.nextInt(0, 100);
        if (randInt < 50) {
            return QueueAction.ENQUEUE;
        } else {
            return QueueAction.DEQUEUE;
        }
    }

    /**
     * Join all threads to end simulation.
     */
    public static void joinAllThreads() throws InterruptedException {
        for (Thread t : allThreads) {
            t.join();
        }
        return;
    }


    /**
     * MAIN
     */
    public static void main(String[] args) throws InterruptedException {

        System.out.println("\nLock-Based Queue tests:");

        // Lock-based Queue (array based)
        lbq = new LockBasedQueue(5);

        // Test lock-based queue
        for (int i = 0; i < NUM_THREADS; i++) {
            Thread t = new Thread(new QueueWorker(i, true));
            allThreads.add(t);
            t.start();
        }
        joinAllThreads();

        lbq.printQueue();

        //----------------------------------

        System.out.println("\n\nLock-Free Queue tests:");

        // Lock-free queue (array based)
        lfq = new LockFreeQueue(5);

        // Test lock-free queue
        for (int i = 0; i < NUM_THREADS; i++) {
            Thread t = new Thread(new QueueWorker(i, false));
            allThreads.add(t);
            t.start();
        }

        joinAllThreads();

        lfq.printQueue();

        System.out.println("\n\nDone");
    }
}
