package vitalkrilov.itmo.prog.labs.fiveplus.client.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.EyeColor;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.util.Arrays;
import java.util.List;

class SubprogramCountGreaterThanEyeColor extends SubprogramTemplate {
    SubprogramCountGreaterThanEyeColor(CommandLineWorker commandLineWorker) {
        super(commandLineWorker);
    }

    public Result<Object, String> execute(List<String> args) {
        if (args.size() != 2) {
            return new Result.Err<>("Invalid arguments count");
        }

        EyeColor eyeColor;
        try {
            eyeColor = EyeColor.valueOf(args.get(1));
        } catch (IllegalArgumentException e) {
            return new Result.Err<>(String.format("Incorrect eye color. Use one from these: %s", Arrays.toString(EyeColor.values())));
        }

        var res = this.commandLineWorker.collectionManager.requestCountGreaterThanEyeColor(eyeColor);
        if (res.isErr())
            return new Result.Err<>("Could not get count of elements: " + res.getErr());
        System.out.printf("Found %d elements.%n", res.getOk());
        return new Result.Ok<>();
    }
}