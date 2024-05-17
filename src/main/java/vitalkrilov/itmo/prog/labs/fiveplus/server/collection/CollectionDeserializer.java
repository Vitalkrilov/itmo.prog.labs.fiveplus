package vitalkrilov.itmo.prog.labs.fiveplus.server.collection;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.*;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Logger;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.ValueParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

class CollectionDeserializer extends StdDeserializer<Collection> {
    public CollectionDeserializer() {
        this(null);
    }

    public CollectionDeserializer(Class<?> vc) {
        super(vc);
    }

    private static <T> Result<T, String> notNullOrNotFound(T o) {
        if (o == null)
            return new Result.Err<>("Element not found");
        return new Result.Ok<>(o);
    }

    @Override
    public Collection deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode mainNode = jsonParser.getCodec().readTree(jsonParser);

        Collection collection = new Collection();

        JsonNode creationDateNode = mainNode.get("creationDate");
        if (creationDateNode == null) {
            collection.creationDate = LocalDateTime.now();
            Logger.logWarning("`creationDate` does not exist. Fixing it to be `now()` (-> %s)", collection.creationDate);
        } else {
            String creationDate = creationDateNode.asText();
            try {
                collection.creationDate = LocalDateTime.parse(creationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"));
            } catch (DateTimeParseException e) {
                collection.creationDate = LocalDateTime.now();
                Logger.logWarning("`creationDate` contains wrong date (\"%s\"). Fixing it to be `now()` (-> %s)", creationDate, collection.creationDate);
            }
        }

        int max_Person_id = -1;
        JsonNode data = mainNode.get("data");
        if (data == null) {
            Logger.logWarning("No `data` found so collection will be empty.");
        } else if (data.get("person") != null) {
            Set<Integer> uniqueIdCheck = new HashSet<>();
            JsonNode personsNode = data.get("person");
            for (JsonNode personNode : (personsNode.isArray() ? personsNode : Arrays.stream(new JsonNode[] { personsNode }).toList())) {
                Person person = new Person();
                {
                    var res = notNullOrNotFound(personNode.get("id")).andThenF(JsonNode::asText).andThen(ValueParser::parseInteger).andThen(person::setId);
                    if (res.isErr()) {
                        Logger.logWarning("An error occurred while parsing Person `id`: %s.", res.getErr());
                        continue;
                    }
                }
                {
                    var res = notNullOrNotFound(personNode.get("name")).andThenF(JsonNode::asText).andThen(person::setName);
                    if (res.isErr()) {
                        Logger.logWarning("An error occurred while parsing Person `name`: %s.", res.getErr());
                        continue;
                    }
                }
                {
                    JsonNode coordinatesNode = personNode.get("coordinates");
                    if (coordinatesNode == null) {
                        Logger.logWarning("An error occurred while parsing Person `coordinates`: Coordinates object not found.");
                        continue;
                    }
                    Coordinates coordinates = new Coordinates();
                    {
                        var res = notNullOrNotFound(coordinatesNode.get("x")).andThenF(JsonNode::asText).andThen(ValueParser::parseInteger).andThen(coordinates::setX);
                        if (res.isErr()) {
                            Logger.logWarning("An error occurred while parsing Coordinates `x`: %s", res.getErr());
                            continue;
                        }
                    }
                    {
                        var res = notNullOrNotFound(coordinatesNode.get("y")).andThenF(JsonNode::asText).andThen(ValueParser::parseFloat).andThen(coordinates::setY);
                        if (res.isErr()) {
                            Logger.logWarning("An error occurred while parsing Coordinates `y`: %s", res.getErr());
                            continue;
                        }
                    }
                    person.setCoordinates(coordinates);
                }
                {
                    String creationDate;
                    {
                        var res = notNullOrNotFound(personNode.get("creationDate")).andThenF(JsonNode::asText);
                        if (res.isErr()) {
                            Logger.logWarning("An error occurred while parsing Person `creationDate`: %s.", res.getErr());
                            continue;
                        }
                        creationDate = res.getOk();
                    }
                    try {
                        StringBuilder creationDateBuilder = new StringBuilder(creationDate);
                        while (creationDateBuilder.length() < 29)
                            creationDateBuilder.append('0'); // We need it since after point there might be less numbers than 9...
                        creationDate = creationDateBuilder.toString();
                        person.setCreationDate(LocalDateTime.parse(creationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS")));
                    } catch (DateTimeParseException e) {
                        Logger.logWarning("An error occurred while parsing Person `creationDate`: `creationDate` contains wrong date (\"%s\")", creationDate);
                        continue;
                    }
                }
                {
                    var res = notNullOrNotFound(personNode.get("height")).andThenF(JsonNode::asText).andThen(ValueParser::parseLong).andThen(person::setHeight);
                    if (res.isErr()) {
                        Logger.logWarning("An error occurred while parsing Person `height`: %s.", res.getErr());
                        continue;
                    }
                }
                {
                    JsonNode eyeColorNode = personNode.get("eyeColor");
                    if (eyeColorNode != null) {
                        String eyeColor = eyeColorNode.asText();
                        var res = new Result.Ok<String, String>(eyeColor).andThen(s -> {
                            try {
                                return new Result.Ok<>(EyeColor.valueOf(s));
                            } catch (IllegalArgumentException e) {
                                return new Result.Err<>("Incorrect value");
                            }
                        }).andThen(person::setEyeColor);
                        if (res.isErr()) {
                            Logger.logWarning("An error occurred while parsing Person `eyeColor`: %s.", res.getErr());
                            continue;
                        }
                    } else person.setEyeColor(null);
                }
                {
                    JsonNode hairColorNode = personNode.get("hairColor");
                    if (hairColorNode != null) {
                        String hairColor = hairColorNode.asText();
                        var res = new Result.Ok<String, String>(hairColor).andThen(s -> {
                            try {
                                return new Result.Ok<>(HairColor.valueOf(s));
                            } catch (IllegalArgumentException e) {
                                return new Result.Err<>("Incorrect value");
                            }
                        }).andThen(person::setHairColor);
                        if (res.isErr()) {
                            Logger.logWarning("An error occurred while parsing Person `hairColor`: %s.", res.getErr());
                            continue;
                        }
                    } else person.setHairColor(null);
                }
                {
                    var res = notNullOrNotFound(personNode.get("nationality")).andThenF(JsonNode::asText).andThen(s -> {
                        try {
                            return new Result.Ok<>(Country.valueOf(s));
                        } catch (IllegalArgumentException e) {
                            return new Result.Err<>("Incorrect value");
                        }
                    }).andThen(person::setNationality);
                    if (res.isErr()) {
                        Logger.logWarning("An error occurred while parsing Person `nationality`: %s.", res.getErr());
                        continue;
                    }
                }
                {
                    JsonNode locationNode = personNode.get("location");
                    if (locationNode != null) {
                        Location location = new Location();
                        {
                            var res = notNullOrNotFound(locationNode.get("x")).andThenF(JsonNode::asText).andThen(ValueParser::parseInteger).andThen(location::setX);
                            if (res.isErr()) {
                                Logger.logWarning("An error occurred while parsing Location `x`: %s", res.getErr());
                                continue;
                            }
                        }
                        {
                            var res = notNullOrNotFound(locationNode.get("y")).andThenF(JsonNode::asText).andThen(ValueParser::parseFloat).andThen(location::setY);
                            if (res.isErr()) {
                                Logger.logWarning("An error occurred while parsing Location `y`: %s", res.getErr());
                                continue;
                            }
                        }
                        {
                            var res = notNullOrNotFound(locationNode.get("z")).andThenF(JsonNode::asText).andThen(ValueParser::parseDouble).andThen(location::setZ);
                            if (res.isErr()) {
                                Logger.logWarning("An error occurred while parsing Location `z`: %s", res.getErr());
                                continue;
                            }
                        }
                        person.setLocation(location);
                    } else person.setLocation(null);
                }

                max_Person_id = Math.max(max_Person_id, person.getId());
                if (!uniqueIdCheck.add(person.getId())) {
                    Logger.logWarning("DB contains Person with duplicate IDs (%d). Ignoring the last one...", person.getId());
                    continue;
                }

                collection.data.add(person);
            }
        }

        JsonNode data_Person_id_seqNode = mainNode.get("data_Person_id_seq");
        if (data_Person_id_seqNode == null) {
            collection.data_Person_id_seq = max_Person_id + 1;
            Logger.logWarning("`data_Person_id_seq` does not exist. Generating correct... (-> %d)", collection.data_Person_id_seq);
        } else {
            boolean generated = false;
            try {
                collection.data_Person_id_seq = Integer.parseInt(data_Person_id_seqNode.asText());
            } catch (NumberFormatException e) {
                generated = true;
                collection.data_Person_id_seq = max_Person_id + 1;
                Logger.logWarning("`data_Person_id_seq` is not integer. Generating correct... (-> %d)", collection.data_Person_id_seq);
            }
            if (!generated) {
                if (max_Person_id != -1 && collection.data_Person_id_seq <= max_Person_id) {
                    int oldValue = collection.data_Person_id_seq;
                    collection.data_Person_id_seq = max_Person_id + 1;
                    Logger.logWarning("`data_Person_id_seq` must be greater than `ID` of any `Person`. Generating correct... (%d -> %d)", oldValue, collection.data_Person_id_seq);
                } else if (collection.data_Person_id_seq < 0) {
                    int oldValue = collection.data_Person_id_seq;
                    collection.data_Person_id_seq = max_Person_id + 1;
                    Logger.logWarning("`data_Person_id_seq` must be greater than 0. Generating correct... (%d -> %d)", oldValue, collection.data_Person_id_seq);
                }
            }
        }

        return collection;
    }
}