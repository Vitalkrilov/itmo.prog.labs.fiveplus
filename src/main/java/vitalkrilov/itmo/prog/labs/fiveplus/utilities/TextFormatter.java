package vitalkrilov.itmo.prog.labs.fiveplus.utilities;

/**
 * Very minimal realisation (but enough for this lab.)
 */
public class TextFormatter {
    public enum Color {
        BLACK,
        RED,
        GREEN,
        YELLOW,
        BLUE,
        MAGENTA,
        CYAN,
        WHITE,
        BRIGHT_BLACK,
        BRIGHT_RED,
        BRIGHT_GREEN,
        BRIGHT_YELLOW,
        BRIGHT_BLUE,
        BRIGHT_MAGENTA,
        BRIGHT_CYAN,
        BRIGHT_WHITE
    }

    /**
     * Colors provided text.
     * @param s Text to be colored.
     * @param c Color to be applied.
     * @return Colored text.
     */
    public static String colorify(String s, Color c) {
        return "\u001B[" + switch (c) {
            case BLACK -> "30";
            case RED -> "31";
            case GREEN -> "32";
            case YELLOW -> "33";
            case BLUE -> "34";
            case MAGENTA -> "35";
            case CYAN -> "36";
            case WHITE -> "37";
            case BRIGHT_BLACK -> "90";
            case BRIGHT_RED -> "91";
            case BRIGHT_GREEN -> "92";
            case BRIGHT_YELLOW -> "93";
            case BRIGHT_BLUE -> "94";
            case BRIGHT_MAGENTA -> "95";
            case BRIGHT_CYAN -> "96";
            case BRIGHT_WHITE -> "97";
        } + "m" + s + "\u001B[0m";
    }

    /**
     * Makes first letter of provided string to be in lower case.
     * @param s String to be started with lower case.
     * @return String after this operation.
     */
    public static String startWithLowercase(String s) {
        if (s.isEmpty()) return s;
        return String.valueOf(s.charAt(0)).toLowerCase() + s.substring(1);
    }
}
