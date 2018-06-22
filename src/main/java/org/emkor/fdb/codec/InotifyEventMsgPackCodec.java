package org.emkor.fdb.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.emkor.fdb.model.InotifyEvent;
import org.emkor.fdb.model.SerializationException;

import java.io.IOException;

public class InotifyEventMsgPackCodec {
    private final ObjectMapper mapper;

    public InotifyEventMsgPackCodec(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public byte[] serialize(InotifyEvent event) throws SerializationException {
        try {
            return this.mapper.writeValueAsBytes(event);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new SerializationException();
        }
    }

    public InotifyEvent deserialize(byte[] bytes) throws SerializationException {
        try {
            return this.mapper.readValue(bytes, InotifyEvent.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new SerializationException();
        }
    }
}
