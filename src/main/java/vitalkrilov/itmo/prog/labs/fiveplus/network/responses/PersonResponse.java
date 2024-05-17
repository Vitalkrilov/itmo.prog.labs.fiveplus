package vitalkrilov.itmo.prog.labs.fiveplus.network.responses;

import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.Person;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

public class PersonResponse extends Response {
    public Person person;

    public PersonResponse(int id, Person person) {
        super(id);
        this.person = person;
    }

    @Override
    public Result<Object, String> validate() {
        {
            var res = super.validate();
            if (res.isErr()) return res;
        }
        String msgFormat = "Invalid data in structure " + this.getClass().getSimpleName() + " in field ";
        {
            var res = this.person.validate();
            if (res.isErr()) return new Result.Err<>(msgFormat + "`person`: " + res.getErr());
        }
        return new Result.Ok<>();
    }
}
