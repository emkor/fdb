package org.emkor.fdb.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.emkor.fdb.model.InotifyEvent;
import org.emkor.fdb.model.InotifyEventType;
import org.emkor.fdb.model.SerializationException;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.*;

public class InotifyEventMsgPackCodecTest {
    private ObjectMapper mapper = new ObjectMapper(new MessagePackFactory())
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
    private InotifyEventMsgPackCodec codec = null;
    private InotifyEvent event = null;

    @org.junit.Before
    public void setUp() throws Exception {
        codec = new InotifyEventMsgPackCodec(mapper);
        event = new InotifyEvent(OffsetDateTime.now(ZoneOffset.UTC), Paths.get("/some/file.txt"), InotifyEventType.CREATE);
    }

    @org.junit.Test
    public void serialize() throws Exception {
        try {
            byte[] bytesForm = codec.serialize(event);
            InotifyEvent backToEvent = codec.deserialize(bytesForm);
            assertEquals(event, backToEvent);
        } catch (SerializationException e) {
            e.printStackTrace();
            fail("Exception on serialization: " + e.getMessage());
        }
    }

}