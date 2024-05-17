package vitalkrilov.itmo.prog.labs.fiveplus.network.requests;

public class RequestPrintNameStartsWith extends Request {
    public String nameStart;

    public RequestPrintNameStartsWith(int id, String nameStart) {
        super(id);
        this.nameStart = nameStart;
    }
}