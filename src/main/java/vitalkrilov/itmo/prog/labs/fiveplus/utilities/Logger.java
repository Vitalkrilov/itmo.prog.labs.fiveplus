package vitalkrilov.itmo.prog.labs.fiveplus.utilities;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

// Since I have my own logger, then I will just leave here commented usage example of asked one. They're interchangeable.
//import java.util.logging.Logger;

/**
 * Program's Logger class.
 */
public class Logger {
    //private static final Logger LOGGER = Logger.getLogger("labs-fiveplus");

    /**
     * Outputs colored log messages with newline at the end to stderr.
     * @param logType Log type (will be in square braces).
     * @param color Log's line color.
     * @param format Format of message.
     * @param args Arguments for formatting the message.
     * @return Same PrintStream as printf does.
     */
    public static PrintStream log(@NotNull String logType, TextFormatter.Color color, String format, Object... args) {
        return System.err.printf(TextFormatter.colorify("[" + logType + "] " + format + "%n", color), args);
    }

    /**
     * Outputs error ([ERR]) red message.
     * @param format Format of message.
     * @param args Arguments for formatting the message.
     * @return Same PrintStream as printf does.
     */
    public static PrintStream logError(@NotNull String format, Object... args) {
        return log("ERR", TextFormatter.Color.RED, format, args);
    }

    /**
     * Outputs error ([WARN]) yellow message.
     * @param format Format of message.
     * @param args Arguments for formatting the message.
     * @return Same PrintStream as printf does.
     */
    public static PrintStream logWarning(@NotNull String format, Object... args) {
        return log("WARN", TextFormatter.Color.YELLOW, format, args);
    }

    /**
     * Outputs error ([INFO]) white message.
     * @param format Format of message.
     * @param args Arguments for formatting the message.
     * @return Same PrintStream as printf does.
     */
    public static PrintStream logInfo(@NotNull String format, Object... args) {
        //LOGGER.info("This is an info message");
        return log("INFO", TextFormatter.Color.WHITE, format, args);
    }

    /**
     * Just outputs newlined message to stderr.
     * @param format Format of message.
     * @param args Arguments for formatting the message.
     * @return Same PrintStream as printf does.
     */
    public static PrintStream printError(@NotNull String format, Object... args) {
        return System.err.printf(format + "%n", args);
    }
}
