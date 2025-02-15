package com.github.razorplay.packet_handler.exceptions;

import java.util.Objects;

/**
 * Exception thrown when there is an error during packet serialization or deserialization.
 * This can occur during reading from or writing to packet buffers.
 */
public class PacketSerializationException extends Exception {

    /**
     * Constructs a new PacketSerializationException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     * @throws NullPointerException if message is null
     */
    public PacketSerializationException(String message, Throwable cause) {
        super(Objects.requireNonNull(message, "Message cannot be null"), cause);
    }

    /**
     * Constructs a new PacketSerializationException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @throws NullPointerException if message is null
     */
    public PacketSerializationException(String message) {
        super(Objects.requireNonNull(message, "Message cannot be null"));
    }
}