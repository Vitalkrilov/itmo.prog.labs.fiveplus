package vitalkrilov.itmo.prog.labs.fiveplus.network.requests;

public class RequestRemoveById extends Request {
    public int idToRemove;

    public RequestRemoveById(int id, int idToRemove) {
        super(id);
        this.idToRemove = idToRemove;
    }
}