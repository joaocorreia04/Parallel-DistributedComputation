import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class matrixproduct {
    public static void main(String[] args) {
        // Run tests for different matrix sizes
        runTests1();
        runTests2();
        // runTests3();
    }

    public static void runTests1() {
        System.out.println("Running tests for function 1\n");

        try {
            FileWriter fileWriter = new FileWriter("data/data_java1_test.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (int size = 600; size <= 1000; size += 400) {
                for (int attempt = 1; attempt <= 4; attempt++) {
                    performMultiplication(size, size, bufferedWriter);
                }
            }

            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }

    }

    public static void runTests2() {
        System.out.println("Running tests for function 2\n");

        try {
            FileWriter fileWriter = new FileWriter("data/data_java2_test.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (int size = 600; size <= 3000; size += 400) {
                for (int attempt = 1; attempt <= 4; attempt++) {
                    performLineMultiplication(size, size, bufferedWriter);
                }
            }

            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    // public static void runTests3() {
    //     System.out.println("Running tests for function 3\n");

    //     try {
    //         FileWriter fileWriter = new FileWriter("data/data_java3_test.txt");
    //         BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

    //         for (int size = 4096; size <= 10240; size += 2048) {
    //             int[] array = {128, 256, 516};
    //             for (int blockSize : array) {
    //                 for (int attempt = 1; attempt <= 4; attempt++) {
    //                     performBlockMultiplication(size, size, blockSize, bufferedWriter);
    //                 }
    //             }
    //         }

    //         bufferedWriter.close();
    //     } catch (IOException e) {
    //         System.out.println("Error writing to file: " + e.getMessage());
    //     }
    // }

    public static void performMultiplication(int m_ar, int m_br, BufferedWriter bufferedWriter) {

        double[][] pha = new double[m_ar][m_ar];
        double[][] phb = new double[m_ar][m_ar];
        double[][] phc = new double[m_ar][m_ar];

        for (int i = 0; i < m_ar; i++)
            for (int j = 0; j < m_ar; j++)
                pha[i][j] = 1.0;

        for (int i = 0; i < m_br; i++)
            for (int j = 0; j < m_br; j++)
                phb[i][j] = i + 1;

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < m_ar; i++) {
            for (int j = 0; j < m_br; j++) {
                double temp = 0;
                for (int k = 0; k < m_ar; k++) {
                    temp += pha[i][k] * phb[k][j];
                }
                phc[i][j] = temp;
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time: " + (endTime - startTime) / 1000.0 + " seconds");

        // Write results to the file
        try {
            bufferedWriter.write("Dimensions: " + m_ar);
            bufferedWriter.newLine();
            bufferedWriter.write("Time: " + (endTime - startTime) / 1000.0 + " seconds");
            bufferedWriter.newLine();
            bufferedWriter.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }


    public static void performLineMultiplication(int m_ar, int m_br, BufferedWriter bufferedWriter) {

        double[][] pha = new double[m_ar][m_ar];
        double[][] phb = new double[m_ar][m_ar];
        double[][] phc = new double[m_ar][m_ar];

        for (int i = 0; i < m_ar; i++)
            for (int j = 0; j < m_ar; j++)
                pha[i][j] = 1.0;

        for (int i = 0; i < m_br; i++)
            for (int j = 0; j < m_br; j++)
                phb[i][j] = i + 1;

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < m_ar; i++) {
            for (int k = 0; k < m_ar; k++) {
                for (int j = 0; j < m_br; j++) {
                    phc[i][j] += pha[i][k] * phb[k][j];
                }
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time: " + (endTime - startTime) / 1000.0 + " seconds");

        // Write results to the file
        try {
            bufferedWriter.write("Dimensions: " + m_ar);
            bufferedWriter.newLine();
            bufferedWriter.write("Time: " + (endTime - startTime) / 1000.0 + " seconds");
            bufferedWriter.newLine();
            bufferedWriter.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }

        System.out.println("Result Matrix: ");
        for (int c = 0; c < Math.min(10, m_br); c++) {
            System.out.print((int)phc[0][c] + " ");
        }
        System.out.println();

    }


    public static void performBlockMultiplication(int m_ar, int m_br, int blockSize, BufferedWriter bufferedWriter) {

        double[][] pha = new double[m_ar][m_ar];
        double[][] phb = new double[m_ar][m_ar];
        double[][] phc = new double[m_ar][m_ar];

        for (int i = 0; i < m_ar; i++)
            for (int j = 0; j < m_ar; j++)
                pha[i][j] = 1.0;

        for (int i = 0; i < m_br; i++)
            for (int j = 0; j < m_br; j++)
                phb[i][j] = i + 1;

        long startTime = System.currentTimeMillis();

        for (int a = 0; a < m_ar; a += blockSize) {
            for (int c = 0; c < m_ar; c += blockSize) {
                for (int b = 0; b < m_br; b += blockSize) {
                    for (int i = a; i < a + blockSize; i++) {
                        for (int k = c; k < c + blockSize; k++) {
                            for (int j = b; j < b + blockSize; j++) {
                                phc[i][j] += pha[i][k] * phb[k][j];
                            }
                        }
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time: " + (endTime - startTime) / 1000.0 + " seconds");

        // Write results to the file
        try {
            bufferedWriter.write("Block size: " + blockSize);
            bufferedWriter.newLine();
            bufferedWriter.write("Dimensions: " + m_ar);
            bufferedWriter.newLine();
            bufferedWriter.write("Time: " + (endTime - startTime) / 1000.0 + " seconds");
            bufferedWriter.newLine();
            bufferedWriter.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }

        System.out.println("Result Matrix: ");
        for (int c = 0; c < Math.min(10, m_br); c++) {
            System.out.print((int)phc[0][c] + " ");
        }
        System.out.println();
    }
}
