package ca.mcgill.ecse420.a1;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class MatrixMultiplication {

    /**--- Question 1 ---**/

    public static void main(String[] args) {
        /**
         * Question 1.1, 1.2
         * Implementing sequential and parallel matrix multiplication methods. Please see below.
         */


        /**
         * Question 1.4
         * Plot the execution time versus number of threads for parallel matrix multiplication
         **/
        System.out.println("Question 1.4");
        List<Integer> numThreadsVals = List.of(1, 2, 3, 4, 8, 10, 20, 50);
        double[][] timeData = new double[numThreadsVals.size()][2];

        for (int i = 0; i < numThreadsVals.size(); i++) {
            int numThreads = numThreadsVals.get(i);
            double elapsed = measureExecutionTime(2000, true, numThreads);
            timeData[i][0] = numThreads;
            timeData[i][1] = elapsed;
            System.out.println(numThreads + " thread(s) - " + elapsed);
        }

        System.out.println("Finding best performing number of threads...");
        int bestPerformingNumThreads = (int) findBestPerformingNumThreads(timeData);
        System.out.println("Best number of threads: " + bestPerformingNumThreads);
        System.out.println();

        // plot 1.4
        double[][] empty_matrix = new double[timeData.length][((double[][]) timeData)[0].length];
        Plot plot_1_4 = new Plot("Execution time versus number of threads for 2000 x 2000 parallel matrix multiplication", "Number of threads", "Execution time (ms)", empty_matrix, timeData);
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
        List<Integer> matrixSizes = List.of(100, 200, 500, 1000, 2000, 3000, 4000);
        double[][] timeDataSequential = new double[matrixSizes.size()][2];
        double[][] timeDataParallel = new double[matrixSizes.size()][2];
        System.out.println("Question 1.5");

        for(int i = 0; i < matrixSizes.size(); i++) {
            int size = matrixSizes.get(i);

            System.out.println("Matrix size: " + size);
            // Sequential times
            timeDataSequential[i][0] = size;
            timeDataSequential[i][1] = measureExecutionTime(size, false, 0);

            // Parallel times
            timeDataParallel[i][0] = size;
            timeDataParallel[i][1] = measureExecutionTime(size, true, bestPerformingNumThreads);

        }

        matrixSizes.forEach(size -> {

        });

        System.out.println("1.5 computation done");

        // plot 1.5
        Plot plot_1_5 = new Plot("Execution time as a function of matrix size for both parallel and sequential methods", "Matrix size", "Execution time (ms)", timeDataSequential, timeDataParallel);
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
         * EXPLAINED IN REPORT
         *
         **/
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
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        } else if (a[0].length != b.length){
            return null;
        }
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

        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        } else if (a[0].length != b.length){
            return null;
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(numThreads);

        return forkJoinPool.invoke(new MultiplierRecursiveTask(a, b));
    }


    /**
     * Recursive task to perform fork/join which takes in two matrices and returns the result of their multiplication.
     * This is done by splitting each inputted matrix into even halves (row wise and column wise respectfully) and then
     * computing the multiplication of each of the four combinations of pairs these halves by reinvoking the recursive task.
     * Finally, the results of these individual multiplications are joined together to produce the resulting matrix.
     */
    public static class MultiplierRecursiveTask extends RecursiveTask<double[][]> {
        private final int THRESHOLD = 20;
        private final double[][] a;
        private final double[][] b;

        public MultiplierRecursiveTask(double[][] a, double[][] b) {
            this.a = a;
            this.b = b;
        }

        protected double[][] compute() {
            if (a.length + b[0].length <= THRESHOLD) {
                return sequentialMultiplyMatrix(a, b);
            } else {
                double[][] result = new double[a.length][b[0].length];
                int numRows = a.length / 2;
                int numCols = b[0].length / 2;

                double[][] aSlice1 = Arrays.copyOfRange(a, 0, numRows);
                double[][] aSlice2 = Arrays.copyOfRange(a, numRows, a.length);

                double[][] bSlice1 = getColumns(b, 0, numCols);
                double[][] bSlice2 = getColumns(b, numCols, b[0].length);

                MultiplierRecursiveTask quad1Task = new MultiplierRecursiveTask(aSlice1, bSlice1);
                MultiplierRecursiveTask quad2Task = new MultiplierRecursiveTask(aSlice1, bSlice2);
                MultiplierRecursiveTask quad3Task = new MultiplierRecursiveTask(aSlice2, bSlice1);
                MultiplierRecursiveTask quad4Task = new MultiplierRecursiveTask(aSlice2, bSlice2);
                invokeAll(quad1Task, quad2Task, quad3Task, quad4Task);
                double[][] quad1 = quad1Task.join();
                double[][] quad2 = quad2Task.join();
                double[][] quad3 = quad3Task.join();
                double[][] quad4 = quad4Task.join();

                for(int i=0; i < quad1.length; i++) {
                    System.arraycopy(quad1[i], 0, result[i], 0, quad1[i].length);
                    System.arraycopy(quad2[i], 0, result[i], numCols, quad2[i].length);
                }

                for(int i=0; i < quad3.length; i++) {
                    System.arraycopy(quad3[i], 0, result[i + numRows], 0, quad3[i].length);
                    System.arraycopy(quad4[i], 0, result[i + numRows], numCols, quad4[i].length);
                }

                return result;
            }
        }

        private double[][] getColumns(double[][] arr, int startIdx, int endIdx) {
            double[][] result = new double[arr.length][endIdx - startIdx];
            for(int i = 0; i < arr.length; i++) {
                result[i] = Arrays.copyOfRange(arr[i], startIdx, endIdx);
            }
            return result;
        }
    }

    /**
     * Question 1.3
     * Measure the execution time of both sequential and parallel matrix multiplication implementations for square matrices.
     *
     * @param matrixSize numRows/numCols desired in the generated square matrices
     * @param isParallel indicates which implementation to measure
     * @param numThreads indicates the number of threads to use if using the parallel implementation
     * @return time elapsed for multiplication algorithm to complete
     */
    public static double measureExecutionTime(int matrixSize, boolean isParallel, int numThreads) {
        long start, end;
        double diff;
        double[][] a = generateRandomMatrix(matrixSize, matrixSize);
        double[][] b = generateRandomMatrix(matrixSize, matrixSize);
        start = System.nanoTime();
        if (isParallel) {
            parallelMultiplyMatrix(a, b, numThreads);
        } else {
            sequentialMultiplyMatrix(a, b);
        }
        end = System.nanoTime();
        diff = (end - start)/1e6;
        return diff;
    }

    /**
     * Helper method to find the best performing number of thread for the parallel matrix implementation.
     * Structure of our 2D array:
     * [ [<number_of_thread_used>, <elapsed_time>], ... [] ]
     *
     * @param timeData '2D' array holding the execution times
     * @return the number of thread used
     */
    public static double findBestPerformingNumThreads(double[][] timeData) {
        double min_time = Double.MAX_VALUE;
        double best_num_thread = 0;
        for (double[] timeDatum : timeData) {
            if (timeDatum[1] < min_time) {
                min_time = timeDatum[1];
                best_num_thread = timeDatum[0];
            }
        }
        return best_num_thread;
    }

    /**
     * Helper function to print out the matrices for debugging.
     *
     * @param matrix to print
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
        double[][] matrix = new double[numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                matrix[row][col] = (int) (Math.random() * 10.0);
            }
        }
        return matrix;
    }

}

