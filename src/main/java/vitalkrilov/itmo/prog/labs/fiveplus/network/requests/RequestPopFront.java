package vitalkrilov.itmo.prog.labs.fiveplus.network.requests;

public class RequestPopFront extends Request {
    public boolean returnElement;

    public RequestPopFront(int id, boolean returnElement) {
        super(id);
        this.returnElement = returnElement;
    }
}