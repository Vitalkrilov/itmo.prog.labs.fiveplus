package vitalkrilov.itmo.prog.labs.fiveplus.client.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.Person;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.util.List;

class SubprogramPrintNameStartsWith extends SubprogramTemplate {
    SubprogramPrintNameStartsWith(CommandLineWorker commandLineWorker) {
        super(commandLineWorker);
    }

    public Result<Object, String> execute(List<String> args) {
        if (args.size() != 2) {
            return new Result.Err<>("Invalid arguments count");
        }

        var res = this.commandLineWorker.collectionManager.requestPrintNameStartsWith(args.get(1));
        if (res.isErr())
            return new Result.Err<>("Could not get elements: " + res.getErr());
        List<Person> data = res.getOk();
        if (data.isEmpty()) {
            System.out.println("No elements found matching the condition.");
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