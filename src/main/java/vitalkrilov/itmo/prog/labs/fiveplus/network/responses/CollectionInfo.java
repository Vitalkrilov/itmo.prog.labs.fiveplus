package vitalkrilov.itmo.prog.labs.fiveplus.network.responses;

import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Just data structure for using in CollectionManager to store some summary data from Collection.
 */
public class CollectionInfo implements Serializable {
    public String typename;
    public LocalDateTime creationDate;
    public int data_Person_id_seq;
    public int size;

    public Result<Object, String> validate() {
        String msgFormat = "Invalid data in structure " + this.getClass().getSimpleName() + " in field ";
        if (size < 0) {
            return new Result.Err<>(msgFormat + "`size`: less than 0");
        }
        return new Result.Ok<>();
    }
}