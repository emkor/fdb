package org.emkor.fdb.verticle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import org.emkor.fdb.codec.ModelSerializer;
import org.emkor.fdb.model.InotifyEvent;
import org.emkor.fdb.model.QueueAddress;
import org.emkor.fdb.model.SerializationException;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class FileUpdateHandler extends AbstractVerticle {
    private ModelSerializer<InotifyEvent> codec = new ModelSerializer<>(new ObjectMapper(new MessagePackFactory())
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule()), InotifyEvent.class);
    private String tmpDirectory = null;

    @Override
    public void start() {
        tmpDirectory = initializeTmpDirectory();
        System.out.println("Deploying FileUpdateHandler with " + tmpDirectory + " as temporary directory");
        vertx.eventBus().consumer(QueueAddress.inotify, (Handler<Message<byte[]>>) message -> {
            handleInotifyEvent(message);
        });
    }

    private void handleInotifyEvent(Message<byte[]> message) {
        byte[] eventBytes = message.body();
        try {
            InotifyEvent event = codec.deserialize(eventBytes);
            System.out.println("Got inotify event: " + event.toString());
            if (Files.exists(event.getFilePath())) {
                Path targetPath = Paths.get(tmpDirectory, event.getFilePath().getFileName().toString());
                targetPath = moveFileToCheckIfWritingToItHasEnded(event.getFilePath(), targetPath);
                if (targetPath != null) {
                    System.out.println("Successfully moved file " + event.getFilePath().getFileName().toString() + " under " + targetPath);
                }
            }
        } catch (SerializationException e) {
            System.out.println("Could not deserialize InotifyEvent!");
            e.printStackTrace();
        }
    }

    private Path moveFileToCheckIfWritingToItHasEnded(Path source, Path target) {
        try {
            Files.move(source, target, REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            System.out.println("Could not move " + source + " to tmp dir: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String initializeTmpDirectory() {
        String tmpDirectory = config().getString("tmp_directory");
        new File(tmpDirectory).mkdirs();
        return tmpDirectory;
    }

    @Override
    public void stop() {
        System.out.println("Stopped FileUpdateHandler!");
    }

}
