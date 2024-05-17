package vitalkrilov.itmo.prog.labs.fiveplus.client.commandline;

/**
 * Template for subprograms.
 */
public abstract class SubprogramTemplate implements SubprogramExecutable {
    protected final CommandLineWorker commandLineWorker;

    /**
     * Creates subprogram.
     * @param commandLineWorker Subprogram's command line worker.
     */
    SubprogramTemplate(CommandLineWorker commandLineWorker) {
        this.commandLineWorker = commandLineWorker;
    }
}
