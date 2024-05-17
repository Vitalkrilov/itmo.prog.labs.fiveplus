package vitalkrilov.itmo.prog.labs.fiveplus.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.collection.CollectionManager;
import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.Person;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.NullableLineInputProcessor;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.io.InputStreamReader;
import java.util.List;

class SubprogramUpdate extends SubprogramTemplate {
    SubprogramUpdate(CommandLineWorker commandLineWorker) {
        super(commandLineWorker);
    }

    public Result<Object, String> execute(List<String> args) {
        if (args.size() != 2) {
            return new Result.Err<>("Invalid arguments count");
        }

        int id;
        try {
            id = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            return new Result.Err<>("Provided `id` is not valid integer");
        }

        if (id <= 0)
            return new Result.Err<>("ID must be more than 0");

        {
            var res = this.commandLineWorker.collectionManager.requestHasID(id);
            if (res.isErr()) return new Result.Err<>(res.getErr());
            if (res.getOk().equals(false))
                return new Result.Err<>("Chosen `id` does not exist");
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
            var res = this.commandLineWorker.collectionManager.requestUpdate(id, person);
            if (res.isErr()) return new Result.Err<>(res.getErr());
            String diff = res.getOk();
            if (diff.isEmpty()) {
                System.out.println("Updated element with same values...");
            } else {
                System.out.println("Applied update to element. Difference:");
                System.out.println(diff);
            }
        }
        return new Result.Ok<>();
    }
}