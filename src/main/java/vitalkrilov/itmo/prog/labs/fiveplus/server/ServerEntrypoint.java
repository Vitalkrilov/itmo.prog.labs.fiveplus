package vitalkrilov.itmo.prog.labs.fiveplus.server;

import vitalkrilov.itmo.prog.labs.fiveplus.server.collection.CollectionManager;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Logger;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Messages;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Entry point of Server App.
 */
public class ServerEntrypoint {

    private final static String DATA_FILE_PATH_ENVVAR = "LAB_FIVEPLUS_FILEPATH";

    /**
     * Program entry point.
     * @param args Command like args.
     */
    public static void main(String[] args) {
        // Create List of arguments with command name inside of it.
        List<String> proper_args = new ArrayList<>();
        {
            File f = new File(ServerEntrypoint.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            if (f.isDirectory()) {
                proper_args.add("lab-fiveplus");
            } else {
                proper_args.add(f.getName());
            }
        }
        proper_args.addAll(List.of(args));

        int port = 7777;

        Queue<String> options = new LinkedList<>();
        for (int i = 1; i < proper_args.size();  ++i) {
            switch (proper_args.get(i)) {
                case "-h", "--help": {
                    System.out.print("Options:\n" +
                            "  -h, --help         Print help\n" +
                            "  -p, --port <PORT>  Choose port for server [default: 7777]\n");
                    System.exit(0);
                }
                case "-p", "--port": {
                    options.add(proper_args.get(i));
                    break;
                }
                default: {
                    String option = options.poll();
                    switch (option) {
                        case "-p", "--port": {
                            try {
                                port = Integer.parseInt(proper_args.get(i));
                            } catch (NumberFormatException e) {
                                Logger.logError("Port must be a number.");
                                System.exit(1);
                            }
                            if (port < 0 || port > 65535) {
                                Logger.logError("Chosen port is not with range [0; 65535].");
                                System.exit(1);
                            }
                            break;
                        }
                    }
                }
            }
        }
        if (!options.isEmpty()) {
            Logger.logError("Invalid count of arguments: expected %d more", options.size());
            System.exit(1);
        }

        String dataFilePath = System.getenv(DATA_FILE_PATH_ENVVAR);
        if (dataFilePath == null)
            Logger.logWarning(Messages.getString("ENVIRONMENT"), DATA_FILE_PATH_ENVVAR);

        CollectionManager collectionManager = new CollectionManager(dataFilePath, port);
        collectionManager.start(proper_args);
    }

}
