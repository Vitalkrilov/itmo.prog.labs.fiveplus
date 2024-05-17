package vitalkrilov.itmo.prog.labs.fiveplus.utilities;

import java.io.Reader;
import java.util.function.Function;

/**
 * Input processor which reads lines until it succeeds or user cancels. Empty lines treated as null.
 */
public class NullableLineInputProcessor {
    private final Reader reader;

    public NullableLineInputProcessor(Reader reader) {
        this.reader = reader;
    }

    /**
     * Start line parsing routine.
     * @param isInteractive Is current session interactive (to recognize when we need to prompt user or just take input from script).
     * @param spacingLevel Current absolute spacing from the left side.
     * @param question Prompt for user to get target value.
     * @param allowNull Allow empty strings (as null) or keep asking to re-enter.
     * @param parser Function to pass collected line to.
     * @return Result of called routine.
     * @param <T> Type of value we needed to get.
     */
    public <T> Result<T, String> collectInteractively(boolean isInteractive, int spacingLevel, String question, boolean allowNull, Function<String, Result<T, String>> parser) {
        while (true) {
            if (isInteractive)
                System.out.printf("%sEnter %s: ", " ".repeat(2 * spacingLevel), question);
            var res = collectOnce(allowNull, parser);
            if (res.isErr()) {
                if (res.getErr().equals("Unexpected EOF"))
                    return new Result.Err<>("Operation cancelled");
                if (!isInteractive)
                    return new Result.Err<>(res.getErr());
                System.out.printf("%sAn error occurred while parsing your value: %s. Try again or press [^D] to cancel current operation.%n", " ".repeat(2 * (spacingLevel + 1)), res.getErr());
                continue;
            }
            return res;
        }
    }

    /**
     * Collects line and then returns results without attempts to retry.
     * @param allowNull Allow empty strings (as null) or keep asking to re-enter.
     * @param parser Function to pass collected line to.
     * @return Result of called routine.
     * @param <T> Type of value we needed to get.
     */
    public <T> Result<T, String> collectOnce(boolean allowNull, Function<String, Result<T, String>> parser) {
        var getLineRes = Utils.getLine(this.reader);
        if (getLineRes.isErr())
            return new Result.Err<>(getLineRes.getErr());
        return ValueParser.parseOrNull(getLineRes.getOk(), allowNull, parser);
    }
}