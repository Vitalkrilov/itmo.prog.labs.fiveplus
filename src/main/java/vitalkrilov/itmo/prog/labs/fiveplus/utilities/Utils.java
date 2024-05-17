package vitalkrilov.itmo.prog.labs.fiveplus.utilities;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;

/**
 * Just some utils.
 */
public class Utils {
    public enum GFType {
        READ,
        WRITE
    }

    /**
     * Returns File if it's possible to open it for reading / writing.
     * @param type Requested mode for checks.
     * @param path File path.
     * @return File, if succeeded. Error message, if failed.
     */
    public static Result<File, String> getFile(GFType type, String path) {
        File file = new File(path);

        if (type == GFType.READ && file.canRead() ||
            type == GFType.WRITE && file.canWrite())
            return new Result.Ok<>(file);

        if (!file.exists()) {
            // Output parent-related analysis (can be extended in cycle to go up until first file will be found but not.)
            File parentFile = file.getAbsoluteFile().getParentFile();
            if (parentFile == null || !parentFile.exists()) {
                return new Result.Err<>("Parent directory does not exist");
            } else if (!parentFile.isDirectory()) {
                return new Result.Err<>("Parent node is not a directory");
            } else {
                if (type == GFType.READ) {
                    if (!parentFile.canRead()) {
                        return new Result.Err<>("Could not find file without permissions to read parent directory");
                    } else {
                        return new Result.Err<>("File does not exist");
                    }
                } else if (type == GFType.WRITE) {
                    if (!parentFile.canWrite()) {
                        return new Result.Err<>("Cannot create file");
                    } else {
                        return new Result.Ok<>(file);
                    }
                }
            }
        }

        if (type == GFType.READ)
            return new Result.Err<>("File is not readable");

        // Case of: `if (type == GFType.WRITE)`
        return new Result.Err<>("File is not writable");
    }

    /**
     * Reads line from reader.
     * @param reader Reader object.
     * @return Result of reading.
     */
    public static Result<String, String> getLine(Reader reader) {
        StringBuilder line;
        // Reason of this: there are two options: never get empty strings (on '\n') or never catch '^D'.
        // So this is the third option.
        try {
            line = new StringBuilder();
            int character;
            while (true) {
                character = reader.read();
                if (character == '\n')
                    break;
                if (character == -1 || character == 0x4)
                    return new Result.Err<>("Unexpected EOF");
                line.append((char) character);
            }
        } catch (NoSuchElementException e) {
            if (e.getMessage().equals("No line found"))
                return new Result.Err<>("Unexpected EOF");
            return new Result.Err<>(String.format("Error while getting next line: %s", e.getMessage()));
        } catch (IOException e) {
            return new Result.Err<>("Reading IO error");
        }
        return new Result.Ok<>(line.toString());
    }
}
