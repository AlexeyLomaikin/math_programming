package transpot_exercise.solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConvertUtils {
    public static int[][] doubleListToPrimitiveArray(List<List<Integer>> list) {
        int[][] array = new int[list.size()][];
        for (int i = 0 ; i < array.length; i++) {
            int rowLength = list.get(i).size();
            array[i] = new int[rowLength];
            for (int j = 0; j < rowLength; j++) {
                array[i][j] = list.get(i).get(j);
            }
        }
        return array;
    }

    public static int[] singleListToPrimitiveArray(List<Integer> list) {
        int[] res = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            res[i] = list.get(i);
        }
        return res;
    }

    public static int[][] copyOf(int[][] array) {
        int[][] copy = new int[array.length][];
        for (int i = 0; i < array.length; i++) {
            copy[i] = Arrays.copyOf(array[i], array[i].length);
        }
        return copy;
    }
}
