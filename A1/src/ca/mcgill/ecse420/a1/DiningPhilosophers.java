package ca.mcgill.ecse420.a1;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {

	static final int NUMBER_OF_PHILOSOPHERS = 50;
	static Philosopher[] philosophers = new Philosopher[NUMBER_OF_PHILOSOPHERS];
	static Chopstick[] chopsticks = new Chopstick[NUMBER_OF_PHILOSOPHERS];
	static Lock pickUpChopstickLock = new ReentrantLock();
	
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

	public static class Philosopher implements Runnable {
		int id;
		Chopstick c1;
		Chopstick c2;
		int chopstickOne;
		int chopstickTwo;
		boolean isEating = false; 	// if isEating is false then philosopher is thinking,
									// if isEating is true then philosopher has two chopsticks.

		public Philosopher(int id) {
			this.id = id;

			if (this.id == 0) {
				this.chopstickOne = NUMBER_OF_PHILOSOPHERS - 1;
			}else {
				this.chopstickOne = this.id - 1;
			}

			this.chopstickTwo = this.id;
		}

		public void getChopsticks() {
//			pickUpChopstickLock.lock();
			if (this.id % 2 == 0) {
				System.out.printf("Philosopher %d: attempting to acquire chopstick %d\n", this.id, chopstickTwo);
				try {
					chopsticks[chopstickTwo].pickUp(this.id);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.printf("Philosopher %d: acquired chopstick %d\n", this.id, chopstickTwo);


				System.out.printf("Philosopher %d: attempting to acquire chopstick %d\n", this.id, chopstickOne);
				try {
					chopsticks[chopstickOne].pickUp(this.id);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.printf("Philosopher %d: acquired chopstick %d\n", this.id, chopstickOne);
			} else {
				System.out.printf("Philosopher %d: attempting to acquire chopstick %d\n", this.id, chopstickOne);
				try {
					chopsticks[chopstickOne].pickUp(this.id);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.printf("Philosopher %d: acquired chopstick %d\n", this.id, chopstickOne);


				System.out.printf("Philosopher %d: attempting to acquire chopstick %d\n", this.id, chopstickTwo);
				try {
					chopsticks[chopstickTwo].pickUp(this.id);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.printf("Philosopher %d: acquired chopstick %d\n", this.id, chopstickTwo);
			}



//			pickUpChopstickLock.unlock();

		}

		@Override
		public void run() {
			// acquire chopsticks
			// must first figure out which chopsticks the philosopher can pick up
			this.getChopsticks();

			// eating
			System.out.printf("Philosopher %d is eating\n", this.id);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.printf("Philosopher %d is done eating\n", this.id);

			// release chopsticks
			chopsticks[chopstickOne].putDown();
			chopsticks[chopstickTwo].putDown();

		}

	}

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
