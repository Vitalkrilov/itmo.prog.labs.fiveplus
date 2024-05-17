package vitalkrilov.itmo.prog.labs.fiveplus.client.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.util.List;

class SubprogramRemoveHead extends SubprogramTemplate {
    SubprogramRemoveHead(CommandLineWorker commandLineWorker) {
        super(commandLineWorker);
    }

    public Result<Object, String> execute(List<String> args) {
        if (args.size() != 1) {
            return new Result.Err<>("Invalid arguments count");
        }

        var res = this.commandLineWorker.collectionManager.requestPopFront(true);
        if (res.isErr()) return new Result.Err<>(res.getErr());
        System.out.println("Removed collection's head:");
        System.out.println("=".repeat(10));
        System.out.println(res.getOk().fieldsToFormattedString());
        System.out.println("=".repeat(10));
        return new Result.Ok<>();
    }
}