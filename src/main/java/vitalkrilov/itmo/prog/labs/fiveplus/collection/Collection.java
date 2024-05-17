package vitalkrilov.itmo.prog.labs.fiveplus.collection;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.Person;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 * Main collection class.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Collection {
    LocalDateTime creationDate; // Reason of use: better to store this here since file's metadata can be easily modified without understanding...
    LinkedList<Person> data;
    int data_Person_id_seq; // Reason of use: when we delete object with highest and restart app, app will reuse previously used it. In client-server architecture this situation must be critical.

    /**
     * Creates new collection.
     */
    public Collection() {
        this(LocalDateTime.now(), new LinkedList<>(), 1);
    }

    /**
     * Creates collection using predefined data.
     * @param creationDate Creation date.
     * @param data List to be used in collection.
     * @param data_Person_id_seq Sequence type for Person's id.
     */
    public Collection(LocalDateTime creationDate, LinkedList<Person> data, int data_Person_id_seq) {
        this.creationDate = creationDate;
        this.data = data;
        this.data_Person_id_seq = data_Person_id_seq;
    }

    /**
     * Clones current collection.
     * @return New cloned collection.
     */
    @Override
    public Collection clone() {
        try {
            Collection clone = (Collection) super.clone();

            // Deep clone list
            clone.data = new LinkedList<>(clone.data.stream().map(Person::clone).toList());

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}