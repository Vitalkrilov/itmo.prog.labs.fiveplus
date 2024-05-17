package vitalkrilov.itmo.prog.labs.fiveplus.network.responses;

import java.util.List;

public class LongsResponse extends Response {
    public List<Long> longs;

    public LongsResponse(int id, List<Long> longs) {
        super(id);
        this.longs = longs;
    }
}
