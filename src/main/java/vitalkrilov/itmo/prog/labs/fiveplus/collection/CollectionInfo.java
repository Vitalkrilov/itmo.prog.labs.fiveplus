package vitalkrilov.itmo.prog.labs.fiveplus.collection;

import java.time.LocalDateTime;

/**
 * Just data structure for using in CollectionManager to store some summary data from Collection.
 */
public class CollectionInfo {
    public String typename;
    public LocalDateTime creationDate;
    public int data_Person_id_seq;
    public int size;
}