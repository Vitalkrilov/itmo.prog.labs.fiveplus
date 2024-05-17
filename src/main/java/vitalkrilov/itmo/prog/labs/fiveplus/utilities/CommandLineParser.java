package vitalkrilov.itmo.prog.labs.fiveplus.utilities;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Command line parser for interactive shell.
 */
public class CommandLineParser {
    /**
     * Parser's execution result.
     */
    public enum PCLResult {
        OK,
        ERR_END_OF_FILE,
        ERR_IO_ERROR
    }

    /**
     * This method parses command using Unix Shell Command Language rules.
     * For reference: <a href="https://pubs.opengroup.org/onlinepubs/9699919799/utilities/V3_chap02.html">Shell Command Language | The Open Group</a>.
     * @param r data to parse from
     * @param args full or partially (if got error) filled list this method will add arguments to
     * @return exit result
     */
    public static PCLResult parseCommand(Reader r, List<String> args) {
        // Special tokens
        final char NONE = 0;
        final char BACKSLASH = '\\';
        final char SINGLE_QUOTE = '\'';
        final char DOUBLE_QUOTE = '"';
        final char SPACE = ' ';
        final char TAB = '\t';
        final char NEWLINE = '\n';
        StringBuilder sb = new StringBuilder();
        int quoteEscapeType = 0; // 0, '\'' or '"'
        boolean backslashEscaped = false;
        boolean emptyArgAllowed = false; // Used for catching empty arguments like `""`
        while (true) {
            char c;
            try {
                int readResult = r.read();
                if (readResult == -1) {
                    if (!sb.isEmpty())
                        args.add(sb.toString());
                    return PCLResult.ERR_END_OF_FILE;
                }
                c = (char) readResult;
            } catch (IOException e) {
                if (!sb.isEmpty())
                    args.add(sb.toString());
                return PCLResult.ERR_IO_ERROR;
            }

            if (backslashEscaped) {
                if (quoteEscapeType == DOUBLE_QUOTE) {
                    if (c != BACKSLASH && c != DOUBLE_QUOTE && c != NEWLINE)
                        sb.append(BACKSLASH);
                    sb.append(c);
                    backslashEscaped = false;
                } else if (quoteEscapeType == NONE) {
                    if (c != BACKSLASH && c != SINGLE_QUOTE && c != DOUBLE_QUOTE && c != NEWLINE && c != SPACE && c != TAB)
                        sb.append(BACKSLASH);
                    sb.append(c);
                    backslashEscaped = false;
                }
            } else {
                if (c == BACKSLASH) {
                    if (quoteEscapeType == SINGLE_QUOTE)
                        sb.append(c);
                    else
                        backslashEscaped = true;
                } else if (c == SINGLE_QUOTE || c == DOUBLE_QUOTE) {
                    if (quoteEscapeType == NONE) {
                        quoteEscapeType = c;
                    } else if (quoteEscapeType == c) {
                        quoteEscapeType = NONE;
                        emptyArgAllowed = sb.isEmpty(); // or just true, it does not matter | That's the only situation when it needed: when quote closed but arg is still empty
                    } else {
                        sb.append(c);
                    }
                } else { // Any other symbol
                    if (quoteEscapeType == NONE) {
                        if (c == SPACE || c == TAB) {
                            if (!sb.isEmpty() || emptyArgAllowed)
                                args.add(sb.toString());
                            sb.setLength(0); // Clear buffer
                            emptyArgAllowed = false;
                        } else if (c == NEWLINE) {
                            break;
                        } else {
                            sb.append(c);
                        }
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        if (!sb.isEmpty() || emptyArgAllowed)
            args.add(sb.toString());

        return PCLResult.OK;
    }
}
