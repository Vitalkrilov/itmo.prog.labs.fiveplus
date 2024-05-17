package vitalkrilov.itmo.prog.labs.fiveplus.client;

import vitalkrilov.itmo.prog.labs.fiveplus.client.commandline.CommandLineWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point of Client App.
 */
public class ClientEntrypoint {

    /**
     * Program entry point.
     * @param args Command like args.
     */
    public static void main(String[] args) {
        // Create List of arguments with command name inside of it.
        List<String> proper_args = new ArrayList<>();
        {
            File f = new File(ClientEntrypoint.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            if (f.isDirectory()) {
                proper_args.add("lab-fiveplus");
            } else {
                proper_args.add(f.getName());
            }
        }
        proper_args.addAll(List.of(args));

        System.exit(new CommandLineWorker().run(proper_args));
    }

}
