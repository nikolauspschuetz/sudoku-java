package sudoku;

public class Utils {

    public static boolean isInteger(String n) {
        if (n == null) {
            return false;
        }
        try {
            Integer.parseInt(n);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
