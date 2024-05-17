package vitalkrilov.itmo.prog.labs.fiveplus.client.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.network.responses.CollectionInfo;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

class SubprogramInfo extends SubprogramTemplate {
    SubprogramInfo(CommandLineWorker commandLineWorker) {
        super(commandLineWorker);
    }

    public Result<Object, String> execute(List<String> args) {
        if (args.size() != 1) {
            return new Result.Err<>("Invalid arguments count");
        }

        var res = this.commandLineWorker.collectionManager.requestInfo();
        if (res.isErr())
            return new Result.Err<>("Could not get information about collection: " + res.getErr());
        CollectionInfo info = res.getOk();
        System.out.println("=".repeat(10));
        System.out.printf("Collection's type: %s.%n", info.typename);
        System.out.printf("Collection's creation date: %s.%n", info.creationDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
        System.out.printf("Collection's next Person ID: %s.%n", info.data_Person_id_seq);
        System.out.printf("Collection's size: %s.%n", info.size);
        System.out.println("=".repeat(10));
        return new Result.Ok<>();
    }
}