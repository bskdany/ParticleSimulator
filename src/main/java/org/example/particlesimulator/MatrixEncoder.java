package org.example.particlesimulator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MatrixEncoder {
    public static String encode(double[][] matrix){
        double[] flat = flattenArray(matrix);
        int[] normalized = normalizeDoubleArray(flat);
        String stringData = arrayToString(normalized);
        byte[] compressed = compressData(stringData.getBytes());
        return Base64.getEncoder().encodeToString(compressed);
    }
    public static double[][] decode(String encoded){
        try{
            byte[] bytes = Base64.getDecoder().decode(encoded);
            byte[] decompressed = decompressData(bytes);
            int[] array = stringToArray(new String(decompressed));
            double[] denormalized = deNormalizeIntArray(array);
            return deFlattenArray(denormalized);
        }
        catch (Exception e){
            System.out.println("Invalid input");
            return null;
        }
    }
    private static byte[] compressData(byte[] data) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)) {
                gzipOutputStream.write(data);
            }
            return baos.toByteArray();
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
    }
    private static byte[] decompressData(byte[] compressedData) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
                 GZIPInputStream gzipInputStream = new GZIPInputStream(bais)) {

                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipInputStream.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
            }
            return baos.toByteArray();
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
    }
    private static double[] flattenArray(double[][] matrix) {
        double[] flat = new double[matrix.length * matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                flat[i * matrix.length + j] = matrix[i][j];
            }
        }
        return flat;
    }
    private static double[][] deFlattenArray(double[] array){
        int newSize = (int) Math.sqrt(array.length);
        double[][] matrix = new double[newSize][newSize];
        for (int i = 0; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                matrix[i][j] = array[i*newSize + j];
            }
        }
        return matrix;
    }
    private static int[] normalizeDoubleArray(double[] original) {
        return Arrays.stream(original).mapToInt(val -> {
            return BigDecimal.valueOf(val).add(BigDecimal.valueOf(1)).multiply(BigDecimal.valueOf(100)).intValue();
        }).toArray();
    }
    private static double[] deNormalizeIntArray(int[] array){
        return Arrays.stream(array).mapToDouble(val -> {
            return BigDecimal.valueOf(val).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).subtract(BigDecimal.valueOf(1)).doubleValue();
        }).toArray();
    }
    private static String arrayToString(int[] array){
        // for simplicity I assume each number is 3 digits long
        String output = "";
        for (int entry : array){
            String data = Integer.toString(entry);
            if(data.length() == 1){
                output = output.concat("00");
            } else if (data.length() == 2) {
                output = output.concat("0");
            }
            output = output.concat(data);
        }
        return output;
    }
    private static int[] stringToArray(String input){
        int[] output = new int[49];
        // (each number is 3 digits long)
        for (int i = 0; i < input.length() / 3; i++) {
            output[i] = Integer.parseInt(input.substring(i*3, i*3+3));
        }
        return output;
    }
}

