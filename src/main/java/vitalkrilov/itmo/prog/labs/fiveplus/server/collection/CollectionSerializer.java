package vitalkrilov.itmo.prog.labs.fiveplus.server.collection;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.Person;

import java.io.IOException;

class CollectionSerializer extends StdSerializer<Collection> {

    public CollectionSerializer() {
        this(null);
    }

    public CollectionSerializer(Class<Collection> vc) {
        super(vc);
    }

    @Override
    public void serialize(Collection collection, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeObjectField("creationDate", collection.creationDate);

        jsonGenerator.writeFieldName("data");
        jsonGenerator.writeStartObject();
        jsonGenerator.writeArrayFieldStart("person");
        for (Person p : collection.data) {
            jsonGenerator.writeObject(p);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();

        jsonGenerator.writeObjectField("data_Person_id_seq", collection.data_Person_id_seq);

        jsonGenerator.writeEndObject();
    }
}