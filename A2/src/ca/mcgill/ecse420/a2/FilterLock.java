package ca.mcgill.ecse420.a2;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class FilterLock implements Lock {
    int n;
    AtomicInteger[] level;
    AtomicInteger[] victim;
    ArrayList<Integer> availableIds;
    ThreadLocal<Integer> myId = ThreadLocal.withInitial(() -> -1);

    public FilterLock(int n) {
        this.n = n;
        level = new AtomicInteger[n];
        victim = new AtomicInteger[n];
        availableIds = new ArrayList<>();
        for (int i=0; i<n; i++){
            availableIds.add(i);
            level[i] = new AtomicInteger();
            victim[i] = new AtomicInteger();
        }
    }

    @Override
    public void lock() {
        if (myId.get() == -1) setMyId();
        //System.out.println("\nThread " + myId.get() + " locking...");
        int i = myId.get();
        boolean spin = true;

        for (int L = 1; L < n; L++) {
            level[i].set(L);
            victim[L].set(i);

            do {
                spin = false;
                for (int j=0; j < n; j++) {
                    if (j != i && level[j].get() >= L && victim[L].get() == i) {
                        spin = true;
                        break;
                    }
                }
            } while (spin);
        }
        //System.out.println("\nThread " + myId.get() + " locked.");
    }

    @Override
    public void unlock() {
        //System.out.println("\nThread " + myId.get() + " unlocking...");
        level[myId.get()].set(0);
        //System.out.println("Thread " + myId.get() + " unlocked.");
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

    private void setMyId() {
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
    }
}