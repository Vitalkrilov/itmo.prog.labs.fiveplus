package vitalkrilov.itmo.prog.labs.fiveplus.client.commandline;


import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.util.List;

/**
 * Command's execution interface.
 */
interface SubprogramExecutable {
    /**
     * Command's entry point.
     * @param args List of parsed arguments. First is always command name, if provided.
     * @return Result of execution.
     */
    Result<Object, String> execute(List<String> args);
}