package vitalkrilov.itmo.prog.labs.fiveplus.network.requests;

import vitalkrilov.itmo.prog.labs.fiveplus.network.responses.PersonResponse;
import vitalkrilov.itmo.prog.labs.fiveplus.network.responses.Response;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Logger;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public abstract class Request implements Serializable {
    private final static int BUFFER_SIZE = 10*1024*1024;

    public int id;

    public Request(int id) {
        this.id = id;
    }

    public Result<Object, String> validate() {
        return new Result.Ok<>();
    }

    public Result<Response, String> sendToServer(byte[] buffer, DatagramSocket socket, InetAddress serverAddress, int port, List<Response> responsesStorage) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
        } catch (IOException e) {
            return new Result.Err<>("Serialization error");
        }
        byte[] bytesData = bos.toByteArray();

        DatagramPacket sendPacket = new DatagramPacket(bytesData, bytesData.length, serverAddress, port);
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            return new Result.Err<>("Failed to send packet to server");
        }

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(receivePacket);
            } catch (IOException e) {
                return new Result.Err<>("Timed out connecting to server...");
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(receivePacket.getData());
            try {
                ObjectInputStream ois = new ObjectInputStream(bis);
                if (ois.readObject() instanceof Response r) {
                    var res = r.validate();
                    if (res.isErr())
                        return new Result.Err<>("Got possibly intentionally faked data through network: " + res.getErr());

                    if (r.getID() == this.id) {
                        return new Result.Ok<>(r);
                    } else {
                        Logger.logWarning("Got unexpected response with ID=%d.", r.getID());
                        responsesStorage.add(r);
                    }
                } else {
                    return new Result.Err<>("Got invalid data class during transmission");
                }
            } catch (IOException e) {
                return new Result.Err<>("Deserialization error");
            } catch (ClassNotFoundException e) {
                return new Result.Err<>("Got non-existing data class during transmission");
            }
        }
    }
}