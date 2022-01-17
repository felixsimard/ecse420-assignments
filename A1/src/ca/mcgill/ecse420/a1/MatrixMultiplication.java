package ca.mcgill.ecse420.a1;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class MatrixMultiplication {

    /**--- Question 1 ---**/

    private static final int NUMBER_THREADS = 1;
    private static final int MATRIX_SIZE = 200;

    public static void main(String[] args) {

        // Generate two random matrices, same size
        double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
        double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
        double[][] sequential = sequentialMultiplyMatrix(a, b);
        double[][] parallel = parallelMultiplyMatrix(a, b, NUMBER_THREADS);

        /**
         * Question 1.1, 1.2
         * Implementing sequential and parallel matrix multiplication methods.
         */
        System.out.println("Sequential equals parallel computation? " + Arrays.deepEquals(sequential, parallel));

//        System.out.println("A:");
//        printMatrix(a);
//
//        System.out.println("B:");
//        printMatrix(b);
//
//        System.out.println("Sequential:");
//        printMatrix(sequential);
//
//        System.out.println("Parallel:");
//        printMatrix(parallel);

        /**
         * Question 1.4
         * Plot the execution time versus number of threads for parallel matrix multiplication
         **/
        int maxNumThread = 20; // what is the max number of threads to use?
        double[][] timeData = new double[maxNumThread][2];
        long elapsed;
        System.out.println("Question 1.4");
        for (int t = 1; t <= maxNumThread; t++) {
            elapsed = measureExecutionTime(true, t);
            timeData[t - 1][0] = t;
            timeData[t - 1][1] = elapsed;
            System.out.println(t + " thread(s) - " + elapsed);
        }
        System.out.println("Finding best performing number of threads...");
        int bestPerformingNumThread = (int) findBestPerformingNumThread(timeData);
        System.out.println("Best number of threads: " + bestPerformingNumThread);
        System.out.println("");

        // plot 1.4
        double[][] empty_matrix = new double[timeData.length][timeData[0].length];
        Plot plot_1_4 = new Plot("Execution time versus number of threads for parallel matrix multiplication", "Number of threads", "Execution time (ns)", empty_matrix, timeData);
        plot_1_4.setAlwaysOnTop(true);
        plot_1_4.pack();
        plot_1_4.setSize(800, 600);
        plot_1_4.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        plot_1_4.setVisible(true);

        /**
         * Question 1.5
         * Plot the execution time versus matrix size (sequential & parallel) as size of matrix changes.
         * Use number of threads for which the parallel execution time was minimum in previous plot.
         **/
        int[] matrixSizes = {100, 200, 500, 1000, 2000, 3000, 4000}; //
        double[][] timeDataSequential = new double[matrixSizes.length][2];
        double[][] timeDataParallel = new double[matrixSizes.length][2];
        System.out.println("Question 1.5");
        for (int s = 0; s < matrixSizes.length; s++) {
            System.out.println("Matrix size: " + matrixSizes[s]);
            // Sequential times
            timeDataSequential[s][0] = matrixSizes[s];
			timeDataSequential[s][1] = measureExecutionTime(false, 0);

			// Parallel times
			timeDataParallel[s][0] = matrixSizes[s];
			timeDataParallel[s][1] = measureExecutionTime(true, bestPerformingNumThread);
        }

        // plot 1.5
        Plot plot_1_5 = new Plot("Execution time as a function of matrix size for both parallel and sequential methods", "Matrix size", "Execution time (ns)", timeDataSequential, timeDataParallel);
        plot_1_5.setAlwaysOnTop(true);
        plot_1_5.pack();
        plot_1_5.setSize(800, 600);
        plot_1_5.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        plot_1_5.setVisible(true);


        /**
         * Question 1.6
         * For the generated graphs in 1.4 and 1.5 comment on their shape and possible reasons
         * for the observed behavior.
         *
         * EXPLAIN HERE
         *
         **/

        return;


    }

    /**
     *
     * Question 1.1
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
     *
     * Question 1.2
     * Returns the result of a concurrent matrix multiplication
     * The two matrices are randomly generated
     *
     * @param a is the first matrix
     * @param b is the second matrix
     * @return the result of the multiplication
     */
    public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b, int numThreads) {
        /**
         * 1.2 Explain the parallel tasks defined in your code.
         *
         * For computing the multiplication of two matrices, we can use parallelism by effectively assigning a thread
         * to compute the dot product of a row (i) and column (j) to get the resulting entry at (i, j) in the output
         * matrix.
         *
         * We defined a MultiplierTask class, which implements Runnable, below that hold the computation required by each dispatched thread.
         * Hence, in this present class, we define a fixed thread pool object which returns to us an 'ExecutorService'
         * object through which we can 'execute' all our tasks as threads.
         *
         *
         */
        double[][] c = new double[a.length][b[0].length];
        ExecutorService taskExecutor = Executors.newFixedThreadPool(numThreads);

        for (int row = 0; row < c.length; row++) {
            for (int col = 0; col < c[row].length; col++) {
                taskExecutor.execute(new MultiplierTask(a, b, c, row, col, MATRIX_SIZE));
            }
        }
        // shutdown task executor and wait for all tasks to finish
        taskExecutor.shutdown();

        while (!taskExecutor.isTerminated());

        try {
            if (!taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
                taskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.out.println("Thread pool interrupted exception occurred.");
            taskExecutor.shutdownNow();
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
     * Question 1.3
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
     * Structure of our 2D array:
     * [ [<number_of_thread_used>, <elapsed_time>], ... [] ]
     *
     * @param '2D' array holding the execution times
     * @return the number of thread used
     */
    public static double findBestPerformingNumThread(double[][] timeData) {
        double min_time = Double.MAX_VALUE;
        double best_num_thread = 0;
        for (int i = 0; i < timeData.length; i++) {
            if (timeData[i][1] < min_time) {
                min_time = timeData[i][1];
                best_num_thread = timeData[i][0];
            }
        }
        return best_num_thread;
    }

    /**
     * Helper function to print out the matrices for debugging.
     *
     * @param matrix
     */
    public static void printMatrix(double[][] matrix) {
        // Reference: https://stackoverflow.com/questions/19648240/java-best-way-to-print-2d-array
        System.out.println(Arrays.deepToString(matrix).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
    }

    /**
     * Helper class to plot our results.
     * Reference: https://www.javatpoint.com/jfreechart-line-chart
     */
    private static class Plot extends JFrame {
        private static final long serialVersionUID = 1L;

        public Plot(String title, String x_axis, String y_axis, double[][] series1, double[][] series2) {
            super(title);
            // Create our dataset
            DefaultCategoryDataset dataset = createDataset(series1, series2);
            // Create chart/plot
            JFreeChart chart = ChartFactory.createLineChart(title, x_axis, y_axis, dataset);
            ChartPanel panel = new ChartPanel(chart);
            setContentPane(panel);
        }

        private DefaultCategoryDataset createDataset(double[][] series1, double[][] series2) {
            String s1 = "Sequential";
            String s2 = "Parallel";
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int i = 0; i < series1.length; i++) {
                for (int j = 0; j < series1[i].length; j++) {
                    dataset.addValue(series1[i][1], s1, "" + series1[i][0] + "");
                    dataset.addValue(series2[i][1], s2, "" + series2[i][0] + "");
                }
            }
            return dataset;
        }
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

