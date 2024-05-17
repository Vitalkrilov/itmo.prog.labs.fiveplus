package vitalkrilov.itmo.prog.labs.fiveplus.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.collection.CollectionInfo;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

class SubprogramClearCollection extends SubprogramTemplate {
    SubprogramClearCollection(CommandLineWorker commandLineWorker) {
        super(commandLineWorker);
    }

    public Result<Object, String> execute(List<String> args) {
        if (args.size() != 1) {
            return new Result.Err<>("Invalid arguments count");
        }

        var res = this.commandLineWorker.collectionManager.requestClear();
        if (res.isErr())
            return new Result.Err<>("Could not get clear collection");

        System.out.println("Collection cleared.");
        return new Result.Ok<>();
    }
}