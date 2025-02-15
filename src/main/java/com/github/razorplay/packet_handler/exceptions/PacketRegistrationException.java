package com.github.razorplay.packet_handler.exceptions;

import java.util.Objects;

/**
 * Runtime exception thrown when there is an error during packet registration,
 * typically due to duplicate packet IDs or invalid packet configurations.
 */
public class PacketRegistrationException extends RuntimeException {

    /**
     * Constructs a new PacketRegistrationException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @throws NullPointerException if message is null
     */
    public PacketRegistrationException(String message) {
        super(Objects.requireNonNull(message, "Message cannot be null"));
    }
}