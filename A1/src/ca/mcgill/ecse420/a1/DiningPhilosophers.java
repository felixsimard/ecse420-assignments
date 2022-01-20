package ca.mcgill.ecse420.a1;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {

	static final int NUMBER_OF_PHILOSOPHERS = 5;
	static Philosopher[] philosophers = new Philosopher[NUMBER_OF_PHILOSOPHERS];
	static Chopstick[] chopsticks = new Chopstick[NUMBER_OF_PHILOSOPHERS];
	
	public static void main(String[] args) {

		// will initialize arrays with philosophers and chopsticks
		for (int i = 0; i < NUMBER_OF_PHILOSOPHERS; i++) {
			chopsticks[i] = new Chopstick();
			philosophers[i] = new Philosopher(i);
		}

		// will start all the threads (make philosophers begin eating.
		for (Philosopher p : philosophers) {
			Thread t = new Thread(p);
			t.start();
		}
	}

	public static class Philosopher implements Runnable {
		int id;
		boolean isEating = false; 	// if isEating is false then philosopher is thinking

		public Philosopher(int id) {
			this.id = id;
		}

		@Override
		public void run() {
			// acquire chopsticks
			// must first figure out which chopsticks the philosopher can pick up
			int chopstickOne;
			if (this.id == 0) {
				chopstickOne = NUMBER_OF_PHILOSOPHERS - 1;
			}else {
				chopstickOne = this.id - 1;
			}
			System.out.printf("Philosopher %d: attempting to acquire chopstick %d\n", this.id, chopstickOne);

			try {
				chopsticks[chopstickOne].pickUp(this.id);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.printf("Philosopher %d: acquired chopstick %d\n", this.id, chopstickOne);

			int chopstickTwo = this.id;
			System.out.printf("Philosopher %d: attempting to acquire chopstick %d\n", this.id, chopstickTwo);
			try {
				chopsticks[chopstickTwo].pickUp(this.id);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.printf("Philosopher %d: acquired chopstick %d\n", this.id, chopstickTwo);

			// eating
			System.out.printf("Philosopher %d is eating\n", this.id);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// release chopsticks
			chopsticks[chopstickOne].putDown();
			chopsticks[chopstickTwo].putDown();

		}

	}

	public static class Chopstick {
		private boolean inUse = false;
		int philosopherId = -1; 	// ID of the philosopher who has the chopstick. If -1 no philosopher has it
		public Lock lock = new ReentrantLock();
		public Condition condition = lock.newCondition();

		public Chopstick() {
		}

		/**
		 * This is a blocking method that will wait until the chopstick becomes available to pick up.
		 * @param philosopherId
		 */
		public void pickUp(int philosopherId) throws InterruptedException {
			lock.lock();

			while (this.inUse) {
				condition.await();
			}

			this.philosopherId = philosopherId;
			this.inUse = true;

			lock.unlock();
		}

		public void putDown() {
			lock.lock();

			this.philosopherId = -1;
			this.inUse = false;

			condition.signalAll();

			lock.unlock();
		}

	}
}
