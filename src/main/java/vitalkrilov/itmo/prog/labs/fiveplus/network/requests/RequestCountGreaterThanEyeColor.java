package vitalkrilov.itmo.prog.labs.fiveplus.network.requests;

import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.EyeColor;

public class RequestCountGreaterThanEyeColor extends Request {
    public EyeColor eyeColor;

    public RequestCountGreaterThanEyeColor(int id, EyeColor eyeColor) {
        super(id);
        this.eyeColor = eyeColor;
    }
}