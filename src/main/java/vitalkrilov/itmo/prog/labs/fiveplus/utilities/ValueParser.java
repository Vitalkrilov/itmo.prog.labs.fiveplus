package vitalkrilov.itmo.prog.labs.fiveplus.utilities;

import java.math.BigInteger;
import java.util.function.Function;

/**
 * Parser of basic data types.
 */
public class ValueParser {
    /**
     * Applies provided parser to value.
     * @param value Value to be parsed.
     * @param allowNull Allow empty strings (as null) or keep asking to re-enter.
     * @param parser Function to be applied to provided value.
     * @return Result of parsing. Will throw error if got unexpected null.
     * @param <T> Parsing target type.
     */
    public static <T> Result<T, String> parseOrNull(String value, boolean allowNull, Function<String, Result<T, String>> parser) {
        if (value.isEmpty()) {
            if (allowNull)
                return new Result.Ok<>(null);

            return new Result.Err<>("Null is not allowed here");
        }
        return parser.apply(value);
    }

    /**
     * Parses integer.
     * @param value Value to parse.
     * @return Parsed value. Will throw error if value can not fit in data type bounds or if string was in wrong format.
     */
    public static Result<Integer, String> parseInteger(String value) {
        try {
            return new Result.Ok<>(Integer.valueOf(value));
        } catch (NumberFormatException e) {
            try {
                var check = new BigInteger(value);
            } catch (NumberFormatException e1) {
                return new Result.Err<>("Wrong format");
            }
            return new Result.Err<>(String.format("Couldn't fit entered number in current data type (bounds: [%s; %s])", Integer.MIN_VALUE, Integer.MAX_VALUE));
        }
    }

    /**
     * Parses long.
     * @param value Value to parse.
     * @return Parsed value. Will throw error if value can not fit in data type bounds or if string was in wrong format.
     */
    public static Result<Long, String> parseLong(String value) {
        try {
            return new Result.Ok<>(Long.valueOf(value));
        } catch (NumberFormatException e) {
            try {
                var check = new BigInteger(value);
            } catch (NumberFormatException e1) {
                return new Result.Err<>("Wrong format");
            }
            return new Result.Err<>(String.format("Couldn't fit entered number in current data type (bounds: [%s; %s])", Long.MIN_VALUE, Long.MAX_VALUE));
        }
    }

    /**
     * Parses float. Allows scientific notation. Allow both '.' or ',' for decimals. Alerts if precision loss possibly detected for scientific input.
     * @param value Value to parse.
     * @return Parsed value. Will throw error if value can not fit in data type bounds (for non-scientific) or if string was in wrong format.
     */
    public static Result<Float, String> parseFloat(String value) {
        value = value.replace(',', '.'); // Support for commas
        try {
            Float f = Float.valueOf(value);
            boolean isScientific = value.toLowerCase().contains("e");
            if (!isScientific) {
                if (value.charAt(0) == '.')
                    value = '0' + value;
                else if (value.charAt(value.length() - 1) == '.')
                    value += '0';
                else if (!value.contains("."))
                    value += ".0";
            }
            if (!value.equals(String.valueOf(f))) {
                String analyze;
                if (!isScientific)
                    analyze = "Precision loss detected";
                else
                    analyze = "You've used scientific notation so make sure not to loss precision";
                analyze = String.format("%s (data type bounds: [%s; %s])", analyze, Float.MIN_VALUE, Float.MAX_VALUE);
                if (!isScientific)
                    return new Result.Err<>(analyze);
                Logger.logWarning(analyze + ".");
            }
            return new Result.Ok<>(f);
        } catch (NumberFormatException e) {
            return new Result.Err<>("Wrong format");
        }
    }

    /**
     * Parses double. Allows scientific notation. Allow both '.' or ',' for decimals. Alerts if precision loss possibly detected for scientific input.
     * @param value Value to parse.
     * @return Parsed value. Will throw error if value can not fit in data type bounds (for non-scientific) or if string was in wrong format.
     */
    public static Result<Double, String> parseDouble(String value) {
        value = value.replace(',', '.'); // Support for commas
        try {
            Double f = Double.valueOf(value);
            boolean isScientific = value.toLowerCase().contains("e");
            if (!isScientific) {
                if (value.charAt(0) == '.')
                    value = '0' + value;
                else if (value.charAt(value.length() - 1) == '.')
                    value += '0';
                else if (!value.contains("."))
                    value += ".0";
            }
            if (!value.equals(String.valueOf(f))) {
                String analyze;
                if (!isScientific)
                    analyze = "Precision loss detected";
                else
                    analyze = "You've used scientific notation so make sure not to loss precision";
                analyze = String.format("%s (data type bounds: [%s; %s])", analyze, Double.MIN_VALUE, Double.MAX_VALUE);
                if (!isScientific)
                    return new Result.Err<>(analyze);
                Logger.logWarning(analyze + ".");
            }
            return new Result.Ok<>(f);
        } catch (NumberFormatException e) {
            return new Result.Err<>("Wrong format");
        }
    }
}