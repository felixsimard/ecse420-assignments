package ca.mcgill.ecse420.a3;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedQueue {

    /**
     * Lock-based, array-based queue
     */
    public static class LockBasedQueue {
        ReentrantLock headLock;
        ReentrantLock tailLock;
        AtomicInteger size;
        AtomicInteger headIndex;
        AtomicInteger tailIndex;
        int capacity;
        Object[] items;
        Condition notEmptyCondition, notFullCondition;

        public LockBasedQueue(int capacity) {
            this.capacity = capacity;
            this.items = new Object[capacity];
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
                items[tailIndex.get()] = x;
                if (tailIndex.incrementAndGet() == capacity) tailIndex.set(0);
                if(size.getAndIncrement() == 0) {
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
        public void dequeue() throws InterruptedException {
            boolean wakeEnqueuers = false;
            headLock.lock();
            try {
                while(size.get() == 0) notEmptyCondition.await();
                Object x = items[headIndex.get()];
                items[headIndex.get()] = null;
                if(headIndex.incrementAndGet() == capacity) {
                    headIndex.set(0);
                    wakeEnqueuers = true;
                }
                size.decrementAndGet();
            } finally {
                headLock.unlock();
            }
            if(wakeEnqueuers) {
                headLock.lock();
                try {
                    notFullCondition.signalAll();
                } finally {
                    headLock.unlock();
                }
            }

        }

        public void printQueue() {
            System.out.println("------------------");
            for(int i=0; i < capacity; i++) {
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
    public static class LockFreeQueue {
        AtomicInteger headIndex;
        AtomicInteger tailIndex;
        AtomicInteger size;
        int capacity;
        Object[] items;

        public LockFreeQueue(int capacity) {
            this.capacity = capacity;
            this.items = new Object[capacity];
            this.size = new AtomicInteger(0);
            this.headIndex = new AtomicInteger(0);
            this.tailIndex = new AtomicInteger(0);
        }

        /**
         * Enqueue
         */
        public void enqueue(Object x) {
            while(true) {

            }
        }

        /**
         * Dequeue
         */
        public void dequeue() {

        }

        public void printQueue() {
            System.out.println("------------------");
            for(int i=0; i < capacity; i++) {
                System.out.print(String.format("%s | ", items[i]));
            }
            System.out.println("");
            System.out.println(String.format("Head index: %s", headIndex));
            System.out.println(String.format("Tail index: %s", tailIndex));
        }

    }


    /**
     * MAIN
     */
    public static void main(String[] args) throws InterruptedException {

        // Lock-based Queue (using array)
        LockBasedQueue lbq = new LockBasedQueue(5);
        lbq.enqueue(88);
        lbq.printQueue();
        lbq.dequeue();
        lbq.printQueue();

        // Lock-free queue (using array)
        LockFreeQueue lfq = new LockFreeQueue(5);
        lfq.enqueue(99);
        lfq.printQueue();


    }
}
