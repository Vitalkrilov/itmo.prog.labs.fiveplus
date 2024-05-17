package vitalkrilov.itmo.prog.labs.fiveplus.server.collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.EyeColor;
import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.Person;
import vitalkrilov.itmo.prog.labs.fiveplus.network.responses.*;
import vitalkrilov.itmo.prog.labs.fiveplus.network.requests.*;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Manages all collection-specific commands and logic.
 * If request*() method consumes Object, it will never mutate it. If request*() method returns Object, it's just cloned value. So there is no direct access to collection.
 */
public class CollectionManager {
    private final static int BUFFER_SIZE = 10*1024*1024;

    private String dataFilePath;
    private Collection collection;
    private final int port;

    /**
     * Creates new collection manager.
     * @param dataFilePath Path for database.
     */
    public CollectionManager(String dataFilePath, int port) {
        this.dataFilePath = dataFilePath;
        this.port = port;
    }

    /**
     * Starts collection manager routine.
     * @param args Must be guaranteed to have size > 1: executable name, arguments
     */
    public void start(List<String> args) {
        if (dataFilePath == null) {
            Logger.logWarning("No file data path provided. You will be able to choose it once save will be required.");
            this.collection = new Collection();
        } else {
            var res = this.loadCollection();
            if (res.isErr()) {
                if (res.getErr().equals("File not found")) {
                    Logger.logInfo("No file containing collection found. Creating new one...");
                } else {
                    Logger.logError("An error occurred: %s. Creating new collection!", res.getErr());
                }
                this.collection = new Collection();
            }
        }

        Logger.logInfo("%s's Collection Manager is ready to process data.", args.get(0));

        DatagramChannel channel;
        try {
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(this.port));
        } catch (IOException e) {
            Logger.logError("Failed to start server at port %d.", this.port);
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        Logger.logInfo("UDP-Server launched at port %d.", this.port);

        StringBuilder consoleBytes = new StringBuilder();
        main_loop:
        while (true) {
            InetSocketAddress clientAddress;
            try {
                buffer.clear();
                clientAddress = (InetSocketAddress) channel.receive(buffer);
            } catch (IOException e) {
                Logger.logError("Failed to get data from client.");
                continue;
            }

            if (clientAddress != null) {
                Logger.logInfo("[%s] Incoming connection.", clientAddress);

                buffer.flip();
                byte[] bytes = buffer.array();
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                Request request = null;
                try {
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    if (ois.readObject() instanceof Request r) {
                        var res = r.validate();
                        if (res.isErr())
                            Logger.logError("[%s] Got possibly intentionally faked data through network: %s.", clientAddress, res.getErr());
                        else
                            request = r;
                    } else {
                        Logger.logError("[%s] Got invalid data class during transmission.", clientAddress);
                    }
                } catch (IOException e) {
                    Logger.logError("[%s] Deserialization error.", clientAddress);
                } catch (ClassNotFoundException e) {
                    Logger.logError("[%s] Got non-existing data class during transmission.", clientAddress);
                }

                if (request != null) {
                    if (request instanceof RequestAck) {
                        new Response(request.id).sendToClient(buffer, channel, clientAddress).onErrorF(s -> Logger.logError("[%s] An error occurred during sending response: %s.", clientAddress, s));
                    } else {
                        Logger.logInfo("[%s] Requested %s.", clientAddress, request.getClass().getSimpleName());

                        Result<Response, String> executionResult = new Result.Err<>("Not implemented on server");
                        if (request instanceof RequestAdd r) {
                            var res = this.requestAdd(r.person);
                            if (res.isOk())
                                executionResult = new Result.Ok<>(new PersonResponse(request.id, res.getOk()));
                            else
                                executionResult = new Result.Err<>(res.getErr());
                        } else if (request instanceof RequestAddIfMax r) {
                            var res = this.requestAddIfMax(r.person);
                            if (res.isOk())
                                executionResult = new Result.Ok<>(new PersonResponse(request.id, res.getOk()));
                            else
                                executionResult = new Result.Err<>(res.getErr());
                        } else if (request instanceof RequestClearCollection) {
                            var res = this.requestClear();
                            if (res.isOk())
                                executionResult = new Result.Ok<>(new Response(request.id));
                            else
                                executionResult = new Result.Err<>(res.getErr());
                        } else if (request instanceof RequestCountGreaterThanEyeColor r) {
                            var res = this.requestCountGreaterThanEyeColor(r.eyeColor);
                            if (res.isOk())
                                executionResult = new Result.Ok<>(new Response(request.id).setResult(new Result.Ok<>(res.getOk().toString())));
                            else
                                executionResult = new Result.Err<>(res.getErr());
                        } else if (request instanceof RequestHasID r) {
                            var res = this.requestHasID(r.idToCheck);
                            if (res.isOk())
                                executionResult = new Result.Ok<>(new Response(request.id).setResult(new Result.Ok<>(res.getOk().toString())));
                            else
                                executionResult = new Result.Err<>(res.getErr());
                        } else if (request instanceof RequestInfo) {
                            var res = this.requestInfo();
                            if (res.isOk())
                                executionResult = new Result.Ok<>(new CollectionInfoResponse(request.id, res.getOk()));
                            else
                                executionResult = new Result.Err<>(res.getErr());
                        } else if (request instanceof RequestPrintHeightDescending) {
                            var res = this.requestPrintHeightDescending();
                            if (res.isOk())
                                executionResult = new Result.Ok<>(new LongsResponse(request.id, res.getOk()));
                            else
                                executionResult = new Result.Err<>(res.getErr());
                        } else if (request instanceof RequestPrintNameStartsWith r) {
                            var res = this.requestPrintNameStartsWith(r.nameStart);
                            if (res.isOk())
                                executionResult = new Result.Ok<>(new PersonsResponse(request.id, res.getOk()));
                            else
                                executionResult = new Result.Err<>(res.getErr());
                        } else if (request instanceof RequestRemoveById r) {
                            var res = this.requestRemove(r.idToRemove);
                            if (res.isOk())
                                executionResult = new Result.Ok<>(new Response(request.id));
                            else
                                executionResult = new Result.Err<>(res.getErr());
                        } else if (request instanceof RequestPopFront r) {
                            var res = this.requestPopFront(r.returnElement);
                            if (res.isOk()) {
                                if (r.returnElement)
                                    executionResult = new Result.Ok<>(new PersonResponse(request.id, res.getOk()));
                                else
                                    executionResult = new Result.Ok<>(new Response(request.id));
                            } else {
                                executionResult = new Result.Err<>(res.getErr());
                            }
                        } else if (request instanceof RequestSaveCollection) {
                            var res = this.requestSave();
                            if (res.isOk())
                                executionResult = new Result.Ok<>(new Response(request.id));
                            else
                                executionResult = new Result.Err<>(res.getErr());
                        } else if (request instanceof RequestShow) {
                            var res = this.requestCollectionContent();
                            if (res.isOk())
                                executionResult = new Result.Ok<>(new PersonsResponse(request.id, res.getOk()));
                            else
                                executionResult = new Result.Err<>(res.getErr());
                        } else if (request instanceof RequestUpdate r) {
                            var res = this.requestUpdate(r.idToUpdate, r.person);
                            if (res.isOk())
                                executionResult = new Result.Ok<>(new Response(request.id).setResult(new Result.Ok<>(res.getOk())));
                            else
                                executionResult = new Result.Err<>(res.getErr());
                        }

                        if (executionResult.isOk())
                            executionResult.getOk().sendToClient(buffer, channel, clientAddress).onErrorF(s -> Logger.logError("[%s] An error occurred during sending response: %s.", clientAddress, s));
                        else
                            new Response(request.id).setResult(new Result.Err<>(executionResult.getErr())).sendToClient(buffer, channel, clientAddress).onErrorF(s -> Logger.logError("[%s] An error occurred during sending response: %s.", clientAddress, s));
                    }
                }
            } else {
                try {
                    int n = System.in.available();
                    if (n > 0) {
                        byte[] newBytes = System.in.readNBytes(n);
                        int lastEnd = -1;
                        for (int i = 0; i < newBytes.length; ++i) {
                            if (newBytes[i] == '\n') {
                                lastEnd = i;
                                for (int j = 0; j < i; ++j) {
                                    consoleBytes.append((char) newBytes[j]);
                                }
                                String line = consoleBytes.toString();
                                consoleBytes.setLength(0);

                                List<String> command = new ArrayList<>();
                                CommandLineParser.parseCommand(new StringReader(line), command);
                                Result<Object, String> res = new Result.Ok<>();
                                if (!command.isEmpty()) {
                                    switch (command.get(0)) {
                                        case "help": {
                                            if (command.size() != 1) {
                                                res = new Result.Err<>("Invalid arguments count");
                                                break;
                                            }
                                            System.out.printf("Available commands:%n" +
                                                            "  help%n" +
                                                            "  save%n" +
                                                            "  exit%n");
                                            break;
                                        }
                                        case "save": {
                                            if (command.size() != 1) {
                                                res = new Result.Err<>("Invalid arguments count");
                                                break;
                                            }
                                            res = this.requestSave();
                                            if (res.isErr())
                                                break;

                                            System.out.println("Collection saved.");
                                            break;
                                        }
                                        case "exit": {
                                            if (command.size() != 1) {
                                                res = new Result.Err<>("Invalid arguments count");
                                                break;
                                            }

                                            break main_loop;
                                        }
                                        default: {
                                            Logger.printError("%s: command not found: %s", args.get(0), command.get(0));
                                            break;
                                        }
                                    }
                                }
                                if (res.isErr()) {
                                    Logger.printError("%s: error: %s", command.get(0), TextFormatter.startWithLowercase(res.getErr()));
                                }
                            }
                        }
                        for (int i = lastEnd + 1; i < newBytes.length; ++i) {
                            consoleBytes.append((char) newBytes[i]);
                        }
                    }
                } catch (IOException e) {
                    Logger.logError("Console IO Error: %s.", e);
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        }

        try {
            channel.close();
        } catch (IOException e) {
            Logger.logError("Failed to close server channel.");
        }
    }

    /**
     * Adds element to collection.
     * @param p (Will not mutate it) Person to add.
     * @return Cloned Person object or null (if not added).
     */
    private Person addToCollection(Person p) {
        Person toBeAdded = p.clone(); // Reason of clone: store object that can't be changed outside.

        toBeAdded.setId(this.collection.data_Person_id_seq++);
        toBeAdded.setCreationDate(LocalDateTime.now());
        if (this.collection.data.add(toBeAdded))
            return toBeAdded.clone(); // Reason of clone: store object that can't be changed outside.
        return null;
    }

    /**
     * Loads collection. Does not do integrity checks as task does not require it (and also "to provide better adjustments possibility").
     * @return Error if loading failed.
     */
    private Result<Object, String> loadCollection() {
        var getFileRes = Utils.getFile(Utils.GFType.READ, this.dataFilePath);
        if (getFileRes.isErr())
            return new Result.Err<>(getFileRes.getErr());
        File f = getFileRes.getOk();

        byte[] contents = new byte[1024];
        String xmlData;
        try {
            BufferedInputStream fr = new BufferedInputStream(new FileInputStream(f));
            int bytesRead;
            StringBuilder sb = new StringBuilder();
            while ((bytesRead = fr.read(contents)) != -1) {
                sb.append(new String(contents, 0, bytesRead));
            }
            xmlData = sb.toString();
        } catch (FileNotFoundException e) {
            Logger.logError("Race condition detected."); // Because we have already checked this file for existence
            return new Result.Err<>("File not found");
        } catch (IOException e) {
            return new Result.Err<>("Reading error");
        }

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        SimpleModule sm = new SimpleModule();
        sm.addDeserializer(Collection.class, new CollectionDeserializer());
        xmlMapper.registerModule(sm);

        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            this.collection = xmlMapper.readValue(xmlData, Collection.class);
        } catch (JsonProcessingException e) {
            String errorMsg = e.getMessage();
            var result = Pattern.compile("(.+?)\\.").matcher(e.getMessage()).results().findFirst();
            String exactError = "";
            if (result.isPresent()) {
                int resultEnd = result.get().end() - 1;
                if (resultEnd > 0)
                    exactError = ": " + errorMsg.substring(0, resultEnd);
            }
            return new Result.Err<>(String.format("File does not contain valid XML data at [line: %d, column: %d]%s", e.getLocation().getLineNr(), e.getLocation().getColumnNr(), exactError));
        }

        return new Result.Ok<>();
    }

    /**
     * Saves collection.
     * @return Error if saving failed.
     */
    private Result<Object, String> saveCollection() {
        while (true) {
            if (this.dataFilePath != null) {
                var getFileRes = Utils.getFile(Utils.GFType.WRITE, this.dataFilePath);
                if (getFileRes.isErr()) {
                    Logger.logWarning("Currently assigned path (`%s`) for DB is broken, reason: %s. Forgetting this path...", this.dataFilePath, getFileRes.getErr());
                    this.dataFilePath = null;
                }
            }

            if (this.dataFilePath == null) {
                System.out.print("So... Now it's time to set up path where to save DB to: ");
                var getLineRes = Utils.getLine(new InputStreamReader(System.in));
                if (getLineRes.isErr())
                    return new Result.Err<>(getLineRes.getErr());
                this.dataFilePath = getLineRes.getOk();
                Logger.logInfo("Newly assigned path: `%s`", this.dataFilePath);
            } else break;
        }
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        SimpleModule sm = new SimpleModule();
        sm.addSerializer(Collection.class, new CollectionSerializer());
        xmlMapper.registerModule(sm);

        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String xmlData;
        try {
            xmlData = xmlMapper.writeValueAsString(this.collection);
        } catch (JsonProcessingException e) {
            Logger.logError("%s", e);
            return new Result.Err<>("An error occurred while serialization DB to XML");
        }

        File tempFile;
        {
            Random random = new Random();
            String charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            int attemptsLeft = 5;
            while (true) {
                int rndTextLen = 8;
                char[] rndText = new char[rndTextLen];
                for (int i = 0; i < rndTextLen; ++i) {
                    rndText[i] = charset.charAt(random.nextInt(charset.length()));
                }
                String tempFileName = this.dataFilePath + '_' + new String(rndText);

                var getFileRes = Utils.getFile(Utils.GFType.WRITE, tempFileName);
                if (getFileRes.isOk()) {
                    tempFile = getFileRes.getOk();
                    break;
                }
                if (--attemptsLeft == 0) {
                    Logger.logWarning("Failed to create temp file. Last error: %s.", getFileRes.getErr());
                    tempFile = null;
                    break;
                }
            }
        }

        if (tempFile == null) {
            System.out.println("Do you want to perform unsafe file saving? (This may corrupt original file by direct writing if any error occurs)");
            var res = new NullableLineInputProcessor(new InputStreamReader(System.in)).collectInteractively(true, 0, "\"y\" if agree; any other if disagree", true, s -> new Result.Ok<>(s));
            if (res.isErr()) return new Result.Err<>(res.getErr());
            String choice = res.getOk();
            if (choice == null || !choice.strip().equals("y")) return new Result.Err<>("Saving cancelled due to inability to create temp file");
        }

        boolean usingTempFile = tempFile != null;

        if (!usingTempFile) {
            var getFileRes = Utils.getFile(Utils.GFType.WRITE, this.dataFilePath);
            if (getFileRes.isErr())
                return new Result.Err<>(getFileRes.getErr());
            tempFile = getFileRes.getOk();
        }

        try {
            FileWriter fw = new FileWriter(tempFile);
            fw.write(xmlData);
            fw.close();
        } catch (IOException e) {
            return new Result.Err<>("Failed to write to the file. Try again (most-likely it was race condition!)");
        }

        if (usingTempFile) {
            var getFileRes = Utils.getFile(Utils.GFType.WRITE, this.dataFilePath);
            if (getFileRes.isErr())
                return new Result.Err<>(getFileRes.getErr());
            File mainFile = getFileRes.getOk();
            if (!tempFile.renameTo(mainFile)) {
                if (!tempFile.delete())
                    Logger.logError("Failed to delete temp file (race condition!).");
                return new Result.Err<>("Failed to replace main file with temp file (most-likely it was race condition!)");
            }
        }

        return new Result.Ok<>();
    }

    /**
     * NOTE: id and creationData will be updated to be server-side confirmed.
     * @param p Person data to be added.
     * @return Person data that was added (with actual full information).
     */
    public Result<Person, String> requestAdd(Person p) {
        Person addedPerson = this.addToCollection(p);
        if (addedPerson == null)
            return new Result.Err<>("Failed to add provided element to collection");
        return new Result.Ok<>(addedPerson);
    }

    public Result<CollectionInfo, String> requestInfo() {
        CollectionInfo info = new CollectionInfo();
        info.typename = this.collection.data.getClass().getSimpleName();
        info.creationDate = this.collection.creationDate;
        info.data_Person_id_seq = this.collection.data_Person_id_seq;
        info.size = this.collection.data.size();
        return new Result.Ok<>(info);
    }

    /**
     * Get all elements collection contains.
     * @return List of elements...
     */
    public Result<List<Person>, String> requestCollectionContent() {
        // In normal languages and situations where server is not required it could give view, but due to mutability etc., we do this.
        return new Result.Ok<>(this.collection.data.stream().sorted((p1, p2) -> {
            int compareResult;

            if (p1.getLocation() == p2.getLocation())
                compareResult = 0;
            else if (p1.getLocation() == null)
                compareResult = -1;
            else if (p2.getLocation() == null)
                compareResult = 1;
            else
                compareResult = p1.getLocation().compareTo(p2.getLocation());
            if (compareResult != 0)
                return compareResult;

            compareResult = p1.compareTo(p2);

            return compareResult;
        }).map(Person::clone).toList());
    }

    /**
     * @param id Person ID which should be updated.
     * @param p Person data to be added.
     * @return Difference between objects or error reason (if id does not exist).
     */
    public Result<String, String> requestUpdate(int id, Person p) {
        for (int i = 0; i < this.collection.data.size(); ++i) {
            Person collectionView = this.collection.data.get(i);
            if (collectionView.getId().equals(id)) {
                Person toBeAdded = p.clone(); // Reason of clone: store object that can't be changed outside.
                toBeAdded.setId(id);
                toBeAdded.setCreationDate(collectionView.getCreationDate());
                this.collection.data.set(i, toBeAdded);
                return new Result.Ok<>(collectionView.diff(toBeAdded));
            }
        }
        return new Result.Err<>("Provided `id` not found so nothing was updated");
    }

    /**
     * @param id Person ID which should be removed.
     * @return Error (if id not found).
     */
    public Result<Object, String> requestRemove(int id) {
        for (int i = 0; i < this.collection.data.size(); ++i) {
            Person collectionView = this.collection.data.get(i);
            if (collectionView.getId().equals(id)) {
                this.collection.data.remove(collectionView);
                return new Result.Ok<>();
            }
        }
        return new Result.Err<>("Provided `id` not found so nothing was remove");
    }

    /**
     * Fully clears collection. Does not reset any sequence due to security.
     * @return Basically should not drop any error (reserved for future.).
     */
    public Result<Object, String> requestClear() {
        this.collection.data.clear();
        return new Result.Ok<>();
    }

    /**
     * @param returnElement Use true if you need to get popped value.
     * @return Error if collection is empty.
     */
    public Result<Person, String> requestPopFront(boolean returnElement) {
        if (this.collection.data.isEmpty())
            return new Result.Err<>("*Unexpectedly* could not remove value from empty collection");
        Person p = this.collection.data.remove(0);
        if (!returnElement)
            p = null;
        return new Result.Ok<>(p);
    }

    /**
     * Adds provided element to collection if it is greater than max element.
     * NOTE: id and creationData will be updated to be server-side confirmed.
     * @param p Person data to be added.
     * @return Person data that was added (with actual full information).
     */
    public Result<Person, String> requestAddIfMax(Person p) {
        if (!this.collection.data.isEmpty()) {
            Person collectionMax = this.collection.data.stream().max(Person::compareIndividualsTo).get();
            if (collectionMax.compareIndividualsTo(p) >= 0) { // collectionMax >= p
                return new Result.Err<>("Element is not greater than collection's max value");
            }
        }

        Person addedPerson = this.addToCollection(p);
        if (addedPerson == null)
            return new Result.Err<>("Failed to add provided element to collection");
        return new Result.Ok<>(addedPerson);
    }

    /**
     * Counts objects that has greater EyeColor. If object has no EyeColor, then it will not be compared (null v any_or_null = false).
     * @param eyeColor EyeColor elements should have greater than to be counted.
     * @return Basically should not drop any error (reserved for future.).
     */
    public Result<Long, String> requestCountGreaterThanEyeColor(EyeColor eyeColor) {
        long count = (int) this.collection.data.stream().filter(p -> (p.getEyeColor() != null && p.getEyeColor().compareTo(eyeColor) > 0)).count();

        return new Result.Ok<>(count);
    }

    /**
     * Get a list of Person which have `nameStart` at their names beginnings.
     * @param nameStart String that Person's name should start with.
     * @return Basically should not drop any error (reserved for future.).
     */
    public Result<List<Person>, String> requestPrintNameStartsWith(String nameStart) {
        List<Person> res = this.collection.data.stream().filter(p -> p.getName().startsWith(nameStart)).sorted((p1, p2) -> {
            int compareResult;

            if (p1.getLocation() == p2.getLocation())
                compareResult = 0;
            else if (p1.getLocation() == null)
                compareResult = -1;
            else if (p2.getLocation() == null)
                compareResult = 1;
            else
                compareResult = p1.getLocation().compareTo(p2.getLocation());
            if (compareResult != 0)
                return compareResult;

            compareResult = p1.compareTo(p2);

            return compareResult;
        }).map(Person::clone).toList();

        return new Result.Ok<>(res);
    }

    /**
     * Get a list of height fields of all collection's elements sorted in descending order.
     * @return Basically should not drop any error (reserved for future.).
     */
    public Result<List<Long>, String> requestPrintHeightDescending() {
        List<Long> res = this.collection.data.stream().map(Person::getHeight).sorted(Collections.reverseOrder()).toList();

        return new Result.Ok<>(res);
    }

    public Result<Boolean, String> requestHasID(int id) {
        for (Person p : this.collection.data)
            if (p.getId() == id)
                return new Result.Ok<>(true);
        return new Result.Ok<>(false);
    }

    /**
     * Saves collection.
     * @return Error if saving failed.
     */
    public Result<Object, String> requestSave() {
        return this.saveCollection();
    }
}
