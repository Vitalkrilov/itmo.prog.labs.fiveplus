package vitalkrilov.itmo.prog.labs.fiveplus.client.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

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
            return new Result.Err<>("Could not get clear collection: " + res.getErr());

        System.out.println("Collection cleared.");
        return new Result.Ok<>();
    }
}