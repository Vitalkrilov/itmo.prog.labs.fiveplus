package vitalkrilov.itmo.prog.labs.fiveplus.network.responses;

import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.Person;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.util.List;

public class PersonsResponse extends Response {
    public List<Person> persons;

    public PersonsResponse(int id, List<Person> persons) {
        super(id);
        this.persons = persons;
    }

    @Override
    public Result<Object, String> validate() {
        {
            var res = super.validate();
            if (res.isErr()) return res;
        }
        String msgFormat = "Invalid data in structure " + this.getClass().getSimpleName() + " in field ";
        for (int i = 0; i < this.persons.size(); ++i) {
            var res = this.persons.get(i).validate();
            if (res.isErr()) return new Result.Err<>(msgFormat + "`persons[" + i + "]`: " + res.getErr());
        }
        return new Result.Ok<>();
    }
}
