package vitalkrilov.itmo.prog.labs.fiveplus.client.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.Person;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.NullableLineInputProcessor;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.io.InputStreamReader;
import java.util.List;

/**
 * Adds new element (asked from user) to collection.
 */
class SubprogramAdd extends SubprogramTemplate {
    SubprogramAdd(CommandLineWorker commandLineWorker) {
        super(commandLineWorker);
    }

    public Result<Object, String> execute(List<String> args) {
        if (args.size() != 1) {
            return new Result.Err<>("Invalid arguments count");
        }

        Person person = new Person();
        while (true) {
            if (this.commandLineWorker.isInteractiveNow())
                System.out.println("| You can use [^D] to cancel input operation. |");

            {
                var res = person.fillFromReader(this.commandLineWorker.isInteractiveNow(), this.commandLineWorker.sessions.peek().reader);
                if (res.isErr()) {
                    // Newline for Ctrl+D (if interactive).
                    if (this.commandLineWorker.isInteractiveNow() && res.getErr().equals("Operation cancelled"))
                        System.out.println();
                    return new Result.Err<>(res.getErr());
                }
            }

            if (!this.commandLineWorker.isInteractiveNow())
                break;
            {
                var res = new NullableLineInputProcessor(new InputStreamReader(System.in)).collectInteractively(true, 0, "anything to confirm; \"n\" to retry", true, s -> new Result.Ok<>(s));
                if (res.isErr()) return new Result.Err<>(res.getErr());
                String choice = res.getOk();
                if (choice == null || !choice.strip().equals("n"))
                    break;
                System.out.println("Retrying...");
            }
        }
        {
            var res = this.commandLineWorker.collectionManager.requestAdd(person);
            if (res.isErr()) return new Result.Err<>(res.getErr());
            System.out.printf("Added to collection: %s.%n", res.getOk().toString());
        }
        return new Result.Ok<>();
    }
}