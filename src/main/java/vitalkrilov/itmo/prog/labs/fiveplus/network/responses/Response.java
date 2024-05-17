package vitalkrilov.itmo.prog.labs.fiveplus.network.responses;

import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class Response implements Serializable {
    private final int id;
    private Result<String, String> result;

    public Response(int id) {
        this.id = id;
        this.result = new Result.Ok<>("");
    }

    public Result<Object, String> sendToClient(ByteBuffer buffer, DatagramChannel channel, InetSocketAddress clientAddress) {
        buffer.clear();
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(this);
                oos.flush();
            } catch (IOException e) {
                return new Result.Err<>("Serialization error");
            }
            byte[] bosData = bos.toByteArray();
            if (bosData.length > buffer.remaining()) {
                return new Result.Err<>("Unable to transfer so many data");
            }
            buffer.put(bosData);
        }
        buffer.flip();
        try {
            channel.send(buffer, clientAddress);
        } catch (IOException e) {
            return new Result.Err<>("Failed to send data to client");
        }
        return new Result.Ok<>();
    }

    public Result<Object, String> validate() {
        return new Result.Ok<>();
    }

    public int getID() {
        return this.id;
    }

    public Result<String, String> getResult() {
        return this.result;
    }

    public Response setResult(Result<String, String> result) {
        this.result = result;
        return this;
    }
}