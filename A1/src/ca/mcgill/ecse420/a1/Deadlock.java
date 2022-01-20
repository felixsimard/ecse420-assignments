package ca.mcgill.ecse420.a1;

import java.util.concurrent.locks.*;

public class Deadlock {

    /**
     * This is the lock to obtain access to a certain resource named resource1
     */
    public static Lock resource1 = new ReentrantLock();

    /**
     * This is the lock to obtain access to a certain resource named resource2
     */
    public static Lock resource2 = new ReentrantLock();

    /**
     * This program will create two Threads. The first will run Task1 and the second will
     * run Task2.
     *
     * Due to the design of these tasks, a deadlock will occur.
     * @param args
     */
    public static void main(String[] args) {
        Thread t1 = new Thread(new Task1());
        Thread t2 = new Thread(new Task2());

        t1.start();
        t2.start();
    }

    /**
     * This class should be used to create a Thread that will run Task1.
     * Task1 will attempt to use both resource1 and resource2.
     *
     * It will firstly lock resource1 and then lock resource2.
     *
     * Note that the while loop and sleep are implemented to ensure that
     * a deadlock occurs.
     */
    public static class Task1 implements Runnable {
        @Override
        public void run() {
            while (true) {      // while loop ensures deadlock is eventually reached.
                System.out.println("Task1: waiting for Resource 1");
                resource1.lock();
                System.out.println("Task1: received Resource 1");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Task1: waiting for Resource 2");
                resource2.lock();
                System.out.println("Task1: received Resource 2");
            }
        }
    }

    /**
     * This class should be used to create a Thread that will run Task2.
     * Task1 will attempt to use both resource1 and resource2.
     *
     * It will firstly lock resource2 and then lock resource1.
     *
     * Note that the while loop and sleep are implemented to ensure that
     * a deadlock occurs.
     */
    public static class Task2 implements Runnable {
        @Override
        public void run() {
            while (true) {      // while loop ensures deadlock is eventually reached.
                System.out.println("Task2: waiting for Resource 2");
                resource2.lock();
                System.out.println("Task2: received Resource 2");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Task2: waiting for Resource 1");
                resource1.lock();
                System.out.println("Task2: received Resource 1");
            }
        }
    }
}
