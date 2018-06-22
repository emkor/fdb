package org.emkor.fdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import org.emkor.fdb.codec.InotifyEventMsgPackCodec;
import org.emkor.fdb.model.InotifyEvent;
import org.emkor.fdb.model.QueueAddress;
import org.emkor.fdb.model.SerializationException;
import org.msgpack.jackson.dataformat.MessagePackFactory;


public class FileUpdateHandlerVerticle extends AbstractVerticle {
    private InotifyEventMsgPackCodec codec = new InotifyEventMsgPackCodec(new ObjectMapper(new MessagePackFactory())
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule()));

    @Override
    public void start() {
        System.out.println("Deploying FileUpdateHandlerVerticle!");
        vertx.eventBus().consumer(QueueAddress.inotify, (Handler<Message<byte[]>>) message -> {
            byte[] eventBytes = message.body();
            try {
                InotifyEvent event = codec.deserialize(eventBytes);
                System.out.println("Got inotify event: " + event.toString());
            } catch (SerializationException e) {
                System.out.println("Could not deserialize InotifyEvent!");
                e.printStackTrace();
            }
        });
    }

    @Override
    public void stop() {
        System.out.println("Stopped FileUpdateHandlerVerticle!");
    }

}
