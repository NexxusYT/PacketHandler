package com.github.razorplay.packet_handler.exceptions;

import java.util.Objects;

/**
 * Runtime exception thrown when attempting to process a packet type that is not registered
 * in the packet registry system.
 */
public class PacketNotFoundException extends RuntimeException {

    /**
     * Constructs a new PacketNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @throws NullPointerException if message is null
     */
    public PacketNotFoundException(String message) {
        super(Objects.requireNonNull(message, "Message cannot be null"));
    }
}