package ca.mcgill.ecse420.a3.q2;

import java.util.concurrent.locks.ReentrantLock;

public class FineGrainedAlgorithm {

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

        public boolean remove(T item) {
            int key = item.hashCode();
            Node<T> pred = null;
            Node<T> curr = null;

            try {
                pred = head;
                pred.lock();
                if (pred.next == null) {
                    return false;
                }
                curr = pred.next;
                curr.lock();
                while (curr.key <= key) {
                    if (curr.item == item) {
                        pred.next = curr.next;
                        return true;
                    }
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                return false;
            } finally {
                if (curr != null) curr.unlock();
                pred.unlock();
            }
        }

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
                return false;
            } finally {
                if (curr != null) curr.unlock();
                pred.unlock();
            }
        }

        public boolean contains(T item) {
            int key = item.hashCode();
            Node<T> pred = null;
            Node<T> curr = null;

            try {
                pred = head;
                pred.lock();
                if (pred.next == null) {
                    return false;
                }
                curr = pred.next;
                curr.lock();
                while (curr.key <= key) {
                    if (curr.item == item) {
                        return true;
                    }
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                return false;
            } finally {
                if (curr != null) curr.unlock();
                pred.unlock();
            }
        }
    }

    public static void main(String[] args) {
        FineGrainedSet<Integer> set = new FineGrainedSet<>();
        set.add(0);
        set.add(1);
        set.add(3);
        set.add(4);

        System.out.println(set.contains(9));
    }
}
