package vitalkrilov.itmo.prog.labs.fiveplus;

import vitalkrilov.itmo.prog.labs.fiveplus.collection.CollectionManager;
import vitalkrilov.itmo.prog.labs.fiveplus.commandline.CommandLineWorker;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Logger;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Messages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point of App.
 * Variants: { "5": "39695" }
 */
public class App {

    private final static String DATA_FILE_PATH_ENVVAR = "LAB_FIVEPLUS_FILEPATH";

    /**
     * Program entry point.
     * @param args Command like args.
     */
    public static void main(String[] args) {
        // Create List of arguments with command name inside of it.
        List<String> proper_args = new ArrayList<>();
        {
            File f = new File(App.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            if (f.isDirectory()) {
                proper_args.add("lab-fiveplus");
            } else {
                proper_args.add(f.getName());
            }
        }
        proper_args.addAll(List.of(args));

        String dataFilePath = System.getenv(DATA_FILE_PATH_ENVVAR);
        if (dataFilePath == null)
            Logger.logWarning(Messages.getString("ENVIRONMENT"), DATA_FILE_PATH_ENVVAR);

        CollectionManager collectionManager = new CollectionManager(dataFilePath);
        collectionManager.start(proper_args);

        CommandLineWorker commandLineWorker = new CommandLineWorker(collectionManager);
        commandLineWorker.start(proper_args);
    }

}
