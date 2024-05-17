package vitalkrilov.itmo.prog.labs.fiveplus.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.util.List;

class SubprogramRemoveById extends SubprogramTemplate {
    SubprogramRemoveById(CommandLineWorker commandLineWorker) {
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
            var res = this.commandLineWorker.collectionManager.requestRemove(id);
            if (res.isErr()) return new Result.Err<>(res.getErr());
            System.out.printf("Removed element with id=%d.%n", id);
        }
        return new Result.Ok<>();
    }
}