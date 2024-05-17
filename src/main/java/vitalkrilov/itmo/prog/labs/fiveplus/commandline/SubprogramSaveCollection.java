package vitalkrilov.itmo.prog.labs.fiveplus.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.util.List;

class SubprogramSaveCollection extends SubprogramTemplate {
    SubprogramSaveCollection(CommandLineWorker commandLineWorker) {
        super(commandLineWorker);
    }

    public Result<Object, String> execute(List<String> args) {
        if (args.size() != 1) {
            return new Result.Err<>("Invalid arguments count");
        }

        var res = this.commandLineWorker.collectionManager.requestSave();
        if (res.isErr())
            return new Result.Err<>(res.getErr());

        System.out.println("Collection saved.");
        return new Result.Ok<>();
    }
}