package vitalkrilov.itmo.prog.labs.fiveplus.utilities;

import java.io.Reader;

/**
 * Interface that allows users to "say" if their fields can be filled by prompting user.
 */
public interface ReaderFillable {
    Result<Object, String> fillFromReader(boolean isInteractive, Reader reader, int spacingLevel);

    /**
     * Starts filling routine.
     * @param isInteractive Is current session interactive (to recognize when we need to prompt user or just take input from script).
     * @param reader Current session's reader.
     * @return Result of current operation.
     */
    default Result<Object, String> fillFromReader(boolean isInteractive, Reader reader) {
        return fillFromReader(isInteractive, reader, 0);
    }
}