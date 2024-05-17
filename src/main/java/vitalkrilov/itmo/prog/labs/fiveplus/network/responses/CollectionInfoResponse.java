package vitalkrilov.itmo.prog.labs.fiveplus.network.responses;

import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.Person;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

public class CollectionInfoResponse extends Response {
    public CollectionInfo collectionInfo;

    public CollectionInfoResponse(int id, CollectionInfo collectionInfo) {
        super(id);
        this.collectionInfo = collectionInfo;
    }

    @Override
    public Result<Object, String> validate() {
        {
            var res = super.validate();
            if (res.isErr()) return res;
        }
        String msgFormat = "Invalid data in structure " + this.getClass().getSimpleName() + " in field ";
        {
            var res = this.collectionInfo.validate();
            if (res.isErr()) return new Result.Err<>(msgFormat + "`collectionInfo`: " + res.getErr());
        }
        return new Result.Ok<>();
    }
}
