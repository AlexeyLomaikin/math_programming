package transpot_exercise.solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConvertUtils {
    public static double[][] doubleListToPrimitiveArray(List<List<Double>> list) {
        double[][] array = new double[list.size()][];
        for (int i = 0 ; i < array.length; i++) {
            int rowLength = list.get(i).size();
            array[i] = new double[rowLength];
            for (int j = 0; j < rowLength; j++) {
                array[i][j] = list.get(i).get(j);
            }
        }
        return array;
    }

    public static double[] singleListToPrimitiveArray(List<Double> list) {
        double[] res = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            res[i] = list.get(i);
        }
        return res;
    }

    public static double[][] copyOf(double[][] array) {
        double[][] copy = new double[array.length][];
        for (int i = 0; i < array.length; i++) {
            copy[i] = Arrays.copyOf(array[i], array[i].length);
        }
        return copy;
    }
}
