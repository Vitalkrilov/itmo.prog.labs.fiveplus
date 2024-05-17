package vitalkrilov.itmo.prog.labs.fiveplus.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.util.List;

class SubprogramRemoveFirst extends SubprogramTemplate {
    SubprogramRemoveFirst(CommandLineWorker commandLineWorker) {
        super(commandLineWorker);
    }

    public Result<Object, String> execute(List<String> args) {
        if (args.size() != 1) {
            return new Result.Err<>("Invalid arguments count");
        }

        var res = this.commandLineWorker.collectionManager.requestPopFront(false);
        if (res.isErr()) return new Result.Err<>(res.getErr());
        System.out.println("Removed first element from collection.");
        return new Result.Ok<>();
    }
}