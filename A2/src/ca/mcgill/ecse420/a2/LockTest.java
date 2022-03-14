package ca.mcgill.ecse420.a2;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockTest {

    public static Lock getLock(boolean filterLock, int n) {
        return filterLock ? new FilterLock(n) : new BakeryLock(n);
    }
    static int counter = 0;

    public static void main(String[] args) throws InterruptedException {
        // filter lock
        boolean mutualExclusionTest1 = counterTest(true,true,3000, 3);
        System.out.println("Filter lock provided mutual exclusion: " + mutualExclusionTest1);
        // bakery lock
        boolean mutualExclusionTest2 = counterTest(true,false,3000, 3);
        System.out.println("Bakery lock provided mutual exclusion: " + mutualExclusionTest2);
        // no lock
        boolean mutualExclusionTest3 = counterTest(false,false,3000, 3);
        System.out.println("No lock provided mutual exclusion: " + mutualExclusionTest3);
    }

    /**
     * This function implements the counter test. The caller of this function must ensure that max_value is
     * divisible by num_threads.
     *
     * @param useLock       boolean to indicate whethere to use lock or not
     * @param filterLock    if useLock = true, this boolean indicates whether filter lock or bakery lock is used
     * @param max_value     max value to increment to
     * @param num_threads   number of threads doing the increment operation
     * @return
     */
    public static boolean counterTest(boolean useLock, boolean filterLock, int max_value, int num_threads) throws InterruptedException {
        Lock lock = getLock(filterLock, num_threads);
        Thread[] threads = new Thread[num_threads];
        Lock lock2 = new ReentrantLock();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                int loop = max_value / num_threads;
                while (loop > 0) {
                    if (useLock) lock.lock();
                    counter += 1;
                    try {
                        Thread.sleep(new Random().nextInt(10));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    loop--;
                    if (useLock) lock.unlock();
                }
            }
        };

        for (int i=0; i<num_threads; i++) {
            threads[i] = new Thread(r);
        }

        for (int i=0; i<num_threads; i++) {
            threads[i].start();
        }

        for (int i=0; i<num_threads; i++) {
            threads[i].join();
        }

        System.out.println("Counter is: " + counter);
        boolean returnBool = counter == max_value;
        counter = 0;
        return returnBool;
    }
}
