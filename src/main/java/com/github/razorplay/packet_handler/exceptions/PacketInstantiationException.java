package com.github.razorplay.packet_handler.exceptions;

import java.util.Objects;

/**
 * Exception thrown when there is a failure in instantiating a packet.
 * This typically occurs during packet deserialization when the system
 * fails to create a new instance of a packet class.
 */
public class PacketInstantiationException extends Exception {

    /**
     * Constructs a new PacketInstantiationException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     * @throws NullPointerException if message is null
     */
    public PacketInstantiationException(String message, Throwable cause) {
        super(Objects.requireNonNull(message, "Message cannot be null"), cause);
    }
}