package vitalkrilov.itmo.prog.labs.fiveplus.client.collection;

import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.EyeColor;
import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.Person;
import vitalkrilov.itmo.prog.labs.fiveplus.network.responses.*;
import vitalkrilov.itmo.prog.labs.fiveplus.network.requests.*;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.net.UnknownHostException;

/**
 * Manages all collection-specific commands and logic.
 * If request*() method consumes Object, it will never mutate it. If request*() method returns Object, it's just cloned value. So there is no direct access to collection.
 */
public class CollectionManager {
    private final static int BUFFER_SIZE = 10*1024*1024;
    private final static int MAX_DEFAULT_REQUEST_TIMEOUT = 10000;
    private final static int MAX_CHECK_NETWORK_TIMEOUT = MAX_DEFAULT_REQUEST_TIMEOUT / 3;

    private String dataFilePath;
    private final String hostname;
    private final int port;
    private int nextRequestID;

    private final List<Response> responsesStorage;

    /**
     * Creates new collection manager.
     * @param hostname Address of required host
     * @param port Port of required host
     */
    public CollectionManager(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.nextRequestID = 0;
        this.responsesStorage = new ArrayList<>();
    }

    private Result<Response, String> sendRequest(Request request) {
        byte[] buffer = new byte[BUFFER_SIZE];

        DatagramSocket socket;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            return new Result.Err<>("Failed to open UDP-connection to server.");
        }

        InetAddress serverAddress;
        try {
            serverAddress = InetAddress.getByName(this.hostname);
        } catch (UnknownHostException e) {
            return new Result.Err<>("Chosen unknown host.");
        }

        if (!(request instanceof RequestAck)) {
            try {
                socket.setSoTimeout(MAX_CHECK_NETWORK_TIMEOUT);
            } catch (SocketException e) {
                return new Result.Err<>("Failed to setup check timeout.");
            }
            var res = new RequestAck(this.nextRequestID++).sendToServer(buffer, socket, serverAddress, this.port, this.responsesStorage);
            if (res.isErr())
                return res;
        }

        try {
            socket.setSoTimeout(MAX_DEFAULT_REQUEST_TIMEOUT);
        } catch (SocketException e) {
            return new Result.Err<>("Failed to setup timeout.");
        }
        var res = request.sendToServer(buffer, socket, serverAddress, this.port, this.responsesStorage);

        socket.close();

        return res;
    }

    /**
     * Tests connection to server.
     * @return true, if connection confirmed.
     */
    public Result<Object, String> testConnection() {
        var networkRes = this.sendRequest(new RequestAck(this.nextRequestID++));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        return new Result.Ok<>();
    }

    /**
     * NOTE: id and creationData will be updated to be server-side confirmed.
     * @param p Person data to be added.
     * @return Person data that was added (with actual full information).
     */
    public Result<Person, String> requestAdd(Person p) {
        var networkRes = this.sendRequest(new RequestAdd(this.nextRequestID++, p.clone()));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        if (response instanceof PersonResponse r) {
            return new Result.Ok<>(r.person);
        }
        return new Result.Err<>("Wrong response type.");
    }

    public Result<CollectionInfo, String> requestInfo() {
        var networkRes = this.sendRequest(new RequestInfo(this.nextRequestID++));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        if (response instanceof CollectionInfoResponse r) {
            return new Result.Ok<>(r.collectionInfo);
        }
        return new Result.Err<>("Wrong response type.");
    }

    /**
     * Get all elements collection contains.
     * @return List of elements...
     */
    public Result<List<Person>, String> requestCollectionContent() {
        var networkRes = this.sendRequest(new RequestShow(this.nextRequestID++));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        if (response instanceof PersonsResponse r) {
            return new Result.Ok<>(r.persons);
        }
        return new Result.Err<>("Wrong response type.");
    }

    /**
     * @param id Person ID which should be updated.
     * @param p Person data to be added.
     * @return Difference between objects or error reason (if id does not exist).
     */
    public Result<String, String> requestUpdate(int id, Person p) {
        var networkRes = this.sendRequest(new RequestUpdate(this.nextRequestID++, id, p.clone()));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        return new Result.Ok<>(responseRes.getOk());
    }

    /**
     * @param id Person ID which should be removed.
     * @return Error (if id not found).
     */
    public Result<Object, String> requestRemove(int id) {
        var networkRes = this.sendRequest(new RequestRemoveById(this.nextRequestID++, id));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        return new Result.Ok<>();
    }

    /**
     * Fully clears collection. Does not reset any sequence due to security.
     * @return Basically should not drop any error (reserved for future.).
     */
    public Result<Object, String> requestClear() {
        var networkRes = this.sendRequest(new RequestClearCollection(this.nextRequestID++));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        return new Result.Ok<>();
    }

    /**
     * @param returnElement Use true if you need to get popped value.
     * @return Error if collection is empty.
     */
    public Result<Person, String> requestPopFront(boolean returnElement) {
        var networkRes = this.sendRequest(new RequestPopFront(this.nextRequestID++, returnElement));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        if (!returnElement)
            return new Result.Ok<>();

        if (response instanceof PersonResponse r) {
            return new Result.Ok<>(r.person);
        }
        return new Result.Err<>("Wrong response type.");
    }

    /**
     * Adds provided element to collection if it is greater than max element.
     * NOTE: id and creationData will be updated to be server-side confirmed.
     * @param p Person data to be added.
     * @return Person data that was added (with actual full information).
     */
    public Result<Person, String> requestAddIfMax(Person p) {
        var networkRes = this.sendRequest(new RequestAddIfMax(this.nextRequestID++, p.clone()));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        if (response instanceof PersonResponse r) {
            return new Result.Ok<>(r.person);
        }
        return new Result.Err<>("Wrong response type.");
    }

    /**
     * Counts objects that has greater EyeColor. If object has no EyeColor, then it will not be compared (null v any_or_null = false).
     * @param eyeColor EyeColor elements should have greater than to be counted.
     * @return Basically should not drop any error (reserved for future.).
     */
    public Result<Long, String> requestCountGreaterThanEyeColor(EyeColor eyeColor) {
        var networkRes = this.sendRequest(new RequestCountGreaterThanEyeColor(this.nextRequestID++, eyeColor));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        try {
            return new Result.Ok<>(Long.parseLong(responseRes.getOk()));
        } catch (NumberFormatException e) {
            return new Result.Err<>("Server returned invalid data.");
        }
    }

    /**
     * Get a list of Person which have `nameStart` at their names beginnings.
     * @param nameStart String that Person's name should start with.
     * @return Basically should not drop any error (reserved for future.).
     */
    public Result<List<Person>, String> requestPrintNameStartsWith(String nameStart) {
        var networkRes = this.sendRequest(new RequestPrintNameStartsWith(this.nextRequestID++, nameStart));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        if (response instanceof PersonsResponse r) {
            return new Result.Ok<>(r.persons);
        }
        return new Result.Err<>("Wrong response type.");
    }

    /**
     * Get a list of height fields of all collection's elements sorted in descending order.
     * @return Basically should not drop any error (reserved for future.).
     */
    public Result<List<Long>, String> requestPrintHeightDescending() {
        var networkRes = this.sendRequest(new RequestPrintHeightDescending(this.nextRequestID++));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        if (response instanceof LongsResponse r) {
            return new Result.Ok<>(r.longs);
        }
        return new Result.Err<>("Wrong response type.");
    }

    public Result<Boolean, String> requestHasID(int id) {
        var networkRes = this.sendRequest(new RequestHasID(this.nextRequestID++, id));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        return new Result.Ok<>(Boolean.parseBoolean(responseRes.getOk()));
    }

    /**
     * Saves collection.
     * @return Error if saving failed.
     */
    public Result<Object, String> requestSave() {
        var networkRes = this.sendRequest(new RequestSaveCollection(this.nextRequestID++));

        if (networkRes.isErr())
            return new Result.Err<>(networkRes.getErr());
        Response response = networkRes.getOk();
        var responseRes = response.getResult();
        if (responseRes.isErr())
            return new Result.Err<>(responseRes.getErr());

        return new Result.Ok<>();
    }
}
