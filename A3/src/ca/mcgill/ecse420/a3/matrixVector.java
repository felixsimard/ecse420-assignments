package ca.mcgill.ecse420.a3;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

public class matrixVector {
    static int THRESHOLD = 4;

    public static void main(String args[]) {
        System.out.println("Hello World.");

        int[][] m1 = createRandomMatrix(2000, 2000, 10);
//        prettyPrint(m1);

        int[] v1 = createRandomVector(2000, 10);
//        prettyPrint(v1);


        long startTimeSeq = System.currentTimeMillis();
        int[] r = sequentialMatrixVectorMultiplier(m1, v1);
        long stopTimeSeq = System.currentTimeMillis();

        long startTimePar = System.currentTimeMillis();
        int[] r2 = parallelMatrixVectorMultiplier(m1, v1, 20);
        long stopTimePar = System.currentTimeMillis();

//        prettyPrint(r2);
//        prettyPrint(r);
        System.out.println("Elapsed time for sequential execution is: " + (stopTimeSeq - startTimeSeq) + " milliseconds.");
        System.out.println("Elapsed time for parallel execution is: " + (stopTimePar - startTimePar) + " milliseconds.");
    }

    /**
     * This function multiplies a matrix by a column vector and returns the resulting vector.
     *
     * The computation is done sequentially.
     *
     * @param matrix
     * @param vector
     * @return
     */
    public static int[] sequentialMatrixVectorMultiplier(int[][] matrix, int[] vector) {
        int[] returnVector = new int[vector.length];
        int rowIndex = 0;
        for (int[] row : matrix){
            returnVector[rowIndex] = dotProduct(row, vector);
            rowIndex++;
        }
        return returnVector;
    }

    /**
     * This function implements a matrix vector Recursive Task to be used in the parallel implementation
     * of the matrix * vector method.
     */
    public static class MatrixVectorMultiplierRecursiveTask extends RecursiveTask<int[]> {
        int[][] matrix;
        int[] vector;
        double length;
        int threshold;

        public MatrixVectorMultiplierRecursiveTask(int[][] m, int[] v, int threshold) {
            this.matrix = m;
            this.vector = v;
            this.length = Double.valueOf(matrix.length);
            this.threshold = threshold;
        }

        @Override
        protected int[] compute() {
            int[] returnArray = new int[(int) this.length];
            if (matrix.length <= this.threshold) {
                return sequentialMatrixVectorMultiplier(matrix, vector);
            }


            MatrixVectorMultiplierRecursiveTask t1 = new MatrixVectorMultiplierRecursiveTask(
                    Arrays.copyOfRange(matrix, 0, (int) this.length / 2), vector, threshold);
            MatrixVectorMultiplierRecursiveTask t2 = new MatrixVectorMultiplierRecursiveTask(
                    Arrays.copyOfRange(matrix, (int) this.length / 2, (int) this.length), vector, threshold);

            int[] firstHalf = t1.compute();
            int[] secondHalf = t2.compute();

            System.arraycopy(firstHalf, 0, returnArray,0,(int) this.length / 2);
            System.arraycopy(secondHalf, 0, returnArray, (int) this.length / 2 ,(int) this.length - (int) this.length / 2);
            return returnArray;
        }
    }


    /**
     * The parallel implementation of matrix vector multiplication. Uses a ForJoinPool and recursively
     * divides the computations untill a certain threshold is reached at which point it does sequential execution.
     *
     * @param matrix
     * @param vector
     * @param threshold
     * @return
     */
    public static int[] parallelMatrixVectorMultiplier(int[][] matrix, int[] vector, int threshold) {
        ForkJoinPool pool = new ForkJoinPool();
        MatrixVectorMultiplierRecursiveTask task = new MatrixVectorMultiplierRecursiveTask(matrix, vector, threshold);
        return pool.invoke(task);
    }

    /**
     * This function computes the dot product between two vectors.
     *
     * @param v1
     * @param v2
     * @return
     */
    public static int dotProduct(int[] v1, int[] v2) {
        int result = 0;
        for (int i=0; i < v1.length; i++) {
            result += v1[i] * v2[i];
        }
        return result;
    }

    public static int[][] createRandomMatrix(int height, int width, int MAX) {
        int[][] matrix = new int[height][width];
        for (int[] row : matrix) {
            for (int i=0; i < row.length; i++) {
                Random rn = new Random();
                row[i] = rn.nextInt(MAX);
            }
        }
        return matrix;
    }

    public static int[] createRandomVector(int length, int MAX) {
        int[] vector = new int[length];
        for (int i=0; i < length; i++) {
            Random rn = new Random();
            vector[i] = rn.nextInt(MAX);
        }
        return vector;
    }

    /**
     * This function will print a vector.
     * @param v
     */
    public static void prettyPrint(int[] v) {
        System.out.printf("[");
        for (int i : v) {
            System.out.printf(i + ", ");
        }
        System.out.println("]");
    }

    /**
     * This function will print a matrix.
     * @param m
     */
    public static void prettyPrint(int[][] m) {
        System.out.println("[");
        for (int[] row : m) {
            prettyPrint(row);
        }
        System.out.println("]");
    }
}
