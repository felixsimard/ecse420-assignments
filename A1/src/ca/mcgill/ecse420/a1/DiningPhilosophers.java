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
			chopsticks[i] = new Chopstick(i);
			philosophers[i] = new Philosopher(i);
		}

		// will start all the threads (make philosophers begin eating)
		for (Philosopher p : philosophers) {
			Thread t = new Thread(p);
			t.start();
		}
	}

	/**
	 * Philosopher class implements run method that is an infinite while loop where the philosopher will think for a
	 * random time in between 0 and 1 seconds, acquire the chopsticks they need to eat, and eat for a random time in
	 * between 0 and 1 seconds.
	 */
	public static class Philosopher implements Runnable {
		int id;
		int chopstickOne;
		int chopstickTwo;
		boolean isEating = false; 	// if isEating is false then philosopher is thinking,
									// if isEating is true then philosopher has two chopsticks.

		public Philosopher(int id) {
			this.id = id;

			if (id == 0) {	// this will break the cycle
				this.chopstickOne = NUMBER_OF_PHILOSOPHERS - 1;
				this.chopstickTwo = 0;
			} else {
				this.chopstickOne = this.id;
				this.chopstickTwo = (this.id + 1) % NUMBER_OF_PHILOSOPHERS;
			}
		}

		public void think() {
			System.out.printf("Philosopher %d: is thinking...\n", this.id);
			try {
				Thread.sleep((long) (Math.random() * 1000));	// will think for any time between 0 and 1 seconds
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.printf("Philosopher %d: is done thinking...\n", this.id);
		}

		public void eat() {
			isEating = true;
			System.out.printf("Philosopher %d: is eating...\n", this.id);
			try {
				Thread.sleep((long) (Math.random() * 1000));	// will eat for any time between 0 and 1 seconds
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			isEating = false;
			System.out.printf("Philosopher %d: is done eating...\n", this.id);
		}

		public void getChopsticks() {
			// get chopstick 1
			System.out.printf("Philosopher %d: attempting to acquire chopstick %d\n", this.id, chopstickOne);
			try {
				chopsticks[chopstickOne].pickUp(this.id);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.printf("Philosopher %d: acquired chopstick %d\n", this.id, chopstickOne);

			// get chopstick 2
			System.out.printf("Philosopher %d: attempting to acquire chopstick %d\n", this.id, chopstickTwo);
			try {
				chopsticks[chopstickTwo].pickUp(this.id);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.printf("Philosopher %d: acquired chopstick %d\n", this.id, chopstickTwo);
		}

		@Override
		public void run() {
			while (true) {
				this.think();
				this.getChopsticks();
				this.eat();
				// release chopsticks
				chopsticks[chopstickOne].putDown();
				chopsticks[chopstickTwo].putDown();
			}
		}

	}

	/**
	 * This class represents Chopsticks. Each chopstick has a lock and a condition variable.
	 * The lock is used to control access to the philosopherId and inUse variable.
	 * The condition variable is used in the pick up and put down function to signal to other threads that the
	 * chopstick they need is now availble.
	 */
	public static class Chopstick {
		int id;
		private boolean inUse = false;
		int philosopherId = -1; 	// ID of the philosopher who has the chopstick. If -1 no philosopher has it
		public Lock lock = new ReentrantLock();
		public Condition condition = lock.newCondition();

		public Chopstick(int id) {
			this.id = id;
		}

		/**
		 * This is a blocking method that will wait until the chopstick becomes available to pick up.
		 * @param philosopherId
		 */
		public void pickUp(int philosopherId) throws InterruptedException {
			lock.lock();
			while (this.inUse) {
				System.out.printf("Philosopher %d is blocked\n", philosopherId);
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
