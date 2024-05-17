package vitalkrilov.itmo.prog.labs.fiveplus.client.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.util.List;

class SubprogramPrintHeightDescending extends SubprogramTemplate {
    SubprogramPrintHeightDescending(CommandLineWorker commandLineWorker) {
        super(commandLineWorker);
    }

    public Result<Object, String> execute(List<String> args) {
        if (args.size() != 1) {
            return new Result.Err<>("Invalid arguments count");
        }

        var res = this.commandLineWorker.collectionManager.requestPrintHeightDescending();
        if (res.isErr())
            return new Result.Err<>("Could not get elements: " + res.getErr());
        List<Long> data = res.getOk();
        if (data.isEmpty()) {
            System.out.println("No elements in collection.");
        } else {
            System.out.printf("Result: %s.%n", data.toString());
        }
        return new Result.Ok<>();
    }
}