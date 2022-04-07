package ca.mcgill.ecse420.a3.q2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class FineGrainedAlgorithm {

    public static FineGrainedSet<Integer> testSet;
    public static List<String> actionsTaken = Collections.synchronizedList(new ArrayList<>());

    /**
     * Fine grained set uses hand over hand locking to perform operations. Each node has an individual lock.
     * @param <T>
     */
    public static class FineGrainedSet<T> {

        public Node<T> head;

        public FineGrainedSet() {
            head = new Node<T>();
            head.key = Integer.MIN_VALUE;
            Node<T> tail = new Node<>();
            tail.key = Integer.MAX_VALUE;
            head.next = tail;
        }

        public static class Node<T> {
            public T item;
            public int key;
            public volatile Node<T> next;
            public ReentrantLock lock;

            public Node(T item) {
                this(item, null);
            }

            public Node() {
                this.lock = new ReentrantLock();
            }

            public Node(T item, Node<T> next) {
                this.item = item;
                this.key = item.hashCode();
                this.next = next;
                this.lock = new ReentrantLock();
            }

            public void lock() {
                lock.lock();
            }

            public void unlock() {
                lock.unlock();
            }
        }

        /**
         * Hand over hand locking to remove item from set.
         * @param item
         * @return
         */
        public boolean remove(T item) {
            int key = item.hashCode();
            Node<T> pred = null;
            Node<T> curr = null;

            try {
                pred = head;
                pred.lock();
                if (pred.next == null) {
                    actionsTaken.add("Removed " + item + ": false - " + System.currentTimeMillis());
                    return false;
                }
                curr = pred.next;
                curr.lock();
                while (curr.key <= key) {
                    if (curr.item == item) {
                        pred.next = curr.next;
                        actionsTaken.add("Removed " + item + ": true - " + System.currentTimeMillis());
                        return true;
                    }
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                actionsTaken.add("Removed " + item + ": false - " + System.currentTimeMillis());
                return false;
            } finally {
                if (curr != null) curr.unlock();
                pred.unlock();
            }
        }

        /**
         * Hand over hand locking to add item to set.
         * @param item
         * @return
         */
        public boolean add(T item) {
            int key = item.hashCode();
            Node<T> pred = null;
            Node<T> curr = null;
            Node<T> newNode = new Node<T>(item);

            try {
                pred = head;
                pred.lock();
                if (pred.next == null) {
                    pred.next = newNode;
                    actionsTaken.add("Added " + item + ": true - " + System.currentTimeMillis());
                    return true;
                }
                curr = pred.next;
                curr.lock();
                while (curr.key <= key) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                pred.next = newNode;
                newNode.next = curr;
                actionsTaken.add("Added " + item + ": true - " + System.currentTimeMillis());
                return true;
            } finally {
                if (curr != null) curr.unlock();
                pred.unlock();
            }
        }

        /**
         * Hand over hand locking to check if set contains item.
         * @param item
         * @return
         */
        public boolean contains(T item) {
            int key = item.hashCode();
            Node<T> pred = null;
            Node<T> curr = null;

            try {
                pred = head;
                pred.lock();
                if (pred.next == null) {
                    actionsTaken.add("Contains " + item + ": false - " + System.currentTimeMillis());
                    return false;
                }
                curr = pred.next;
                curr.lock();
                while (curr.key <= key) {
                    if (curr.item == item) {
                        actionsTaken.add("Contains " + item + ": true - " + System.currentTimeMillis());
                        return true;
                    }
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                actionsTaken.add("Contains " + item + ": false - " + System.currentTimeMillis());
                return false;
            } finally {
                if (curr != null) curr.unlock();
                pred.unlock();
            }
        }
    }

    public static enum SetAction {ADD, REMOVE, CONTAINS}

    public static class SetTask<T> implements Runnable {
        public SetAction setAction;

        public SetTask(SetAction setAction) {
            this.setAction = setAction;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                switch (setAction) {
                    case ADD:
                        testSet.add(i);
                        break;
                    case REMOVE:
                        testSet.remove(new Random().nextInt(10));
                        break;
                    case CONTAINS:
                        testSet.contains(new Random().nextInt(10));

                }
            }
        }
    }

    /**
     * Test that contains is successful intertwined with removals.
     * If contains(i) returns false, there must either be no add(i) that precedes it, OR if add(i) precedes it,
     *    remove(i) must also precede it and happen after the most recent add(i).
     *    contains(i) returns false -> ( contains(i) HB add(i) ) OR ( add(i) HB remove(i) HB contains(i) )
     * If contains(i) returns true, then add(i) must precede it AND no remove(i) must happen in between them.
     * @throws InterruptedException
     */
    public static void testContainsMethod() throws InterruptedException {
        testSet = new FineGrainedSet<>();
        Thread adder = new Thread(new SetTask<Integer>(SetAction.ADD));
        Thread remover = new Thread(new SetTask<Integer>(SetAction.REMOVE));
        Thread container = new Thread(new SetTask<Integer>(SetAction.CONTAINS));
        // run adder
        adder.start();
        adder.join();

        //remove and check contains in parallel
        remover.start();
        container.start();
        remover.join();
        container.join();
    }


    public static void main(String[] args) {
        try {
            testContainsMethod();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (String s : actionsTaken) {
            System.out.println(s);
        }
    }
}
