package vitalkrilov.itmo.prog.labs.fiveplus.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.Person;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.util.List;

class SubprogramShow extends SubprogramTemplate {
    SubprogramShow(CommandLineWorker commandLineWorker) {
        super(commandLineWorker);
    }

    public Result<Object, String> execute(List<String> args) {
        if (args.size() != 1) {
            return new Result.Err<>("Invalid arguments count");
        }

        var res = this.commandLineWorker.collectionManager.requestCollectionContent();
        if (res.isErr())
            return new Result.Err<>("Could not get information about collection");
        List<Person> data = res.getOk();
        if (data.isEmpty()) {
            System.out.println("No elements in collection.");
        } else {
            System.out.println("=".repeat(10));
            for (Person p : data) {
                System.out.println(p.fieldsToFormattedString());
                System.out.println("=".repeat(10));
            }
        }
        return new Result.Ok<>();
    }
}