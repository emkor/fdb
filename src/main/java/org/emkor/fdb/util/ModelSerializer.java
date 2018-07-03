package org.emkor.fdb.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.emkor.fdb.model.SerializationException;

import java.io.IOException;

public class ModelSerializer<T> {
    private final ObjectMapper mapper;
    private final Class<T> clazz;

    public ModelSerializer(ObjectMapper mapper, Class<T> clazz) {
        this.mapper = mapper;
        this.clazz = clazz;
    }

    public byte[] serialize(T event) {
        try {
            return this.mapper.writeValueAsBytes(event);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new SerializationException(e.getMessage());
        }
    }

    public T deserialize(byte[] bytes) {
        try {
            return this.mapper.readValue(bytes, clazz);
        } catch (IOException e) {
            e.printStackTrace();
            throw new SerializationException(e.getMessage());
        }
    }
}
