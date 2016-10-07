package transpot_exercise.validation;

public class FieldHelper {

    private FieldHelper() {
    }

    public static boolean isNaturalNumber(String fieldValue) {
        if (!isEmpty(fieldValue)) {
            return fieldValue.matches("[1-9]+[0-9]*");
        }
        return false;
    }

    public static boolean isZero(String fieldValue) {
        if (!isEmpty(fieldValue)) {
            return fieldValue.matches("0+");
        }
        return false;
    }

    public static boolean isEmpty(String fieldValue) {
        return fieldValue.isEmpty() || fieldValue.matches("\\s+");
    }
}
