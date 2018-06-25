package org.emkor.fdb.model;

import java.io.Serializable;

public class SerializationException extends RuntimeException implements Serializable {
    public SerializationException() {
    }

    public SerializationException(String message) {
        super(message);
    }
}
