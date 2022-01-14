package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultiplication {

    private static final int NUMBER_THREADS = 1;
    private static final int MATRIX_SIZE = 2000;

    public static void main(String[] args) {

        // Generate two random matrices, same size
        double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
        double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
        sequentialMultiplyMatrix(a, b);
        parallelMultiplyMatrix(a, b, NUMBER_THREADS);

        // Plot the execution time versus number of threads for parallel matrix multiplication
        int maxNumThread = 100;
        long[][] timeData = new long[maxNumThread][1];
        for (int t = 0; t < maxNumThread; t++) {
			timeData[t][0] = measureExecutionTime(true, t);
        }
        int bestPerformingNumThread = findBestPerformingNumThread(timeData);

        // Plot the execution time versus matrix size (sequential & parallel) as size of matrix changes
        // Use number of threads for which the parallel execution time was minimum in previous plot
        int[] matrixSizes = {100, 200, 500, 1000, 2000, 3000, 4000};
        long[][] timeDataSequential = new long[matrixSizes.length][1];
        long[][] timeDataParallel = new long[matrixSizes.length][1];
        for (int s = 0; s < matrixSizes.length; s++) {
			timeDataSequential[s][0] = measureExecutionTime(false, 0);
			timeDataParallel[s][0] = measureExecutionTime(true, bestPerformingNumThread);
        }

        // PLOT THE GRAPHS!!

    }

    /**
     * Returns the result of a sequential matrix multiplication
     * The two matrices are randomly generated
     *
     * @param a is the first matrix
     * @param b is the second matrix
     * @return the result of the multiplication
     */
    public static double[][] sequentialMultiplyMatrix(double[][] a, double[][] b) {
        double[][] c = new double[a.length][b[0].length];
        for (int row = 0; row < c.length; row++) {
            for (int col = 0; col < c[row].length; col++) {
                for (int i = 0; i < b.length; i++) {
                    c[row][col] += a[row][i] * b[i][col];
                }
            }
        }
        return c;
    }

    /**
     * Returns the result of a concurrent matrix multiplication
     * The two matrices are randomly generated
     *
     * @param a is the first matrix
     * @param b is the second matrix
     * @return the result of the multiplication
     */
    public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b, int numThreads) {
        double[][] c = new double[a.length][b[0].length];
        ExecutorService taskExecutor = Executors.newFixedThreadPool(numThreads);

        for (int row = 0; row < c.length; row++) {
            for (int col = 0; col < c[row].length; col++) {
                taskExecutor.execute(new MultiplierTask(a, b, c, row, col, MATRIX_SIZE));
            }
        }

        // shutdown task executor and wait for all tasks to finish
		taskExecutor.shutdown();
        try {
        	taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
        	System.out.println("Thread pool interrupted exception occurred.");
        	System.exit(-1);
		}

        return c;
    }

    /**
     * Private class for deploying a multiplication task in a Thread.
     * Instances of this class are initialized in parallelMultiplyMatrix().
     */
    private static class MultiplierTask implements Runnable {
        double[][] a;
        double[][] b;
        double[][] c;
        int matrixSize, i, j;

        public MultiplierTask(double[][] a, double[][] b, double[][] c, int row, int col, int matrixSize) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.i = row;
            this.j = col;
            this.matrixSize = matrixSize;
        }

        @Override
        public void run() {
            for (int h = 0; h < matrixSize; h++) {
                c[i][j] += a[i][h] * b[h][j];
            }
        }
    }

    /**
     * Measure the execution time of both sequential and parallel matrix multiplication implementations.
     *
     * @param isParallel indicates which implementation to measure
     * @param numThreads indicates the number of threads to use if using the parallel implementation
     * @return time elapsed for multiplication algorithm to complete
     */
    public static long measureExecutionTime(boolean isParallel, int numThreads) {
        long start, end, diff;
        double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
        double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
        start = System.nanoTime();
        if (isParallel) {
            parallelMultiplyMatrix(a, b, numThreads);
        } else {
            sequentialMultiplyMatrix(a, b);
        }
        end = System.nanoTime();
        diff = end - start;
        return diff;
    }

    /**
     * Helper method to find the best performing number of thread for the parallel matrix implementation.
     *
     * @param
     * @return the number of thread used
     */
    public static int findBestPerformingNumThread(long[][] timeData) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < timeData.length; i++) {
            for (int j = 0; j < timeData[i].length; ) {
				if(timeData[i][j] < min) {
					min = i; // recall, i is the number of thread used
				}
			}
        }
        return min;
    }

    /**
     * Populates a matrix of given size with randomly generated integers between 0-10.
     *
     * @param numRows number of rows
     * @param numCols number of cols
     * @return matrix
     */
    private static double[][] generateRandomMatrix(int numRows, int numCols) {
        double matrix[][] = new double[numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                matrix[row][col] = (double) ((int) (Math.random() * 10.0));
            }
        }
        return matrix;
    }

}
