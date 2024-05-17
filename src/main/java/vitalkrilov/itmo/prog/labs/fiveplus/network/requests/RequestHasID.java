package vitalkrilov.itmo.prog.labs.fiveplus.network.requests;

public class RequestHasID extends Request {
    public int idToCheck;

    public RequestHasID(int id, int idToCheck) {
        super(id);
        this.idToCheck = idToCheck;
    }
}