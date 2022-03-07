package ca.mcgill.ecse420.a2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class BakeryLock implements Lock {
    int n;
    boolean[] flag;
    int[] label;
    ThreadLocal<Integer> myId = ThreadLocal.withInitial(() -> -1);
    ArrayList<Integer> availableIds;

    public BakeryLock(int n) {
        this.n = n;
        flag = new boolean[n];
        label = new int[n];
        availableIds = new ArrayList<>();
        for (int i=0; i<n; i++){
            availableIds.add(i);
        }
    }

    @Override
    public void lock() {


        if (myId.get() == -1) setMyId();

        System.out.println("\nThread " + myId.get() + " locking...");

        int i = myId.get();

        flag[i] = true;

        synchronized (label) {
            label[i] = Arrays.stream(label).max().getAsInt() + 1;
            System.out.println("\nThread " + myId.get() + " got label " + label[i]);
        }

        boolean spin = true;
        do {
            spin = false;
            for (int j=0; j<n; j++) {
                if (j != i && flag[j]) {
                    if (label[j] < label[i]) spin = true;
                    if (label[j] == label[i] && j < i) spin = true;
                }
            }

        } while (spin);

        System.out.println("\nThread " + myId.get() + " locked.");
    }

    @Override
    public void unlock() {
        System.out.println("\nThread " + myId.get() + " unlocking...");
        flag[myId.get()] = false;
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
