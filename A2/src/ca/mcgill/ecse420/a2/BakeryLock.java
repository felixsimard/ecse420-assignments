package ca.mcgill.ecse420.a2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class BakeryLock implements Lock {
    int n;
    AtomicBoolean[] flag;
    AtomicInteger[] label;
    ThreadLocal<Integer> myId = ThreadLocal.withInitial(() -> -1);
    ArrayList<Integer> availableIds;

    public BakeryLock(int n) {
        this.n = n;
        flag = new AtomicBoolean[n];
        label = new AtomicInteger[n];
        availableIds = new ArrayList<>();
        for (int i=0; i<n; i++){
            availableIds.add(i);
            label[i] = new AtomicInteger();
            flag[i] = new AtomicBoolean();
        }
    }

    @Override
    public void lock() {
        if (myId.get() == -1) setMyId();
        //System.out.println("\nThread " + myId.get() + " locking...");
        int i = myId.get();
        flag[i].set(true);

        Comparator<AtomicInteger> comparator = new Comparator<AtomicInteger>() {
            @Override
            public int compare(AtomicInteger o1, AtomicInteger o2) {
                if (o1.get() < o2.get()) return -1;
                if (o1.get() > o2.get()) return 1;
                return 0;
            }
        };
        label[i].set(Arrays.stream(label).max(comparator).get().get() + 1);
        //System.out.println("\nThread " + myId.get() + " got label " + label[i]);

        boolean spin = true;
        do {
            spin = false;
            for (int j=0; j<n; j++) {
                if (j != i && flag[j].get()) {
                    if (label[j].get() < label[i].get()) spin = true;
                    if (label[j].get() == label[i].get() && j < i) spin = true;
                }
            }

        } while (spin);
        //System.out.println("\nThread " + myId.get() + " locked.");
    }

    @Override
    public void unlock() {
        //System.out.println("\nThread " + myId.get() + " unlocking...");
        flag[myId.get()].set(false);
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
