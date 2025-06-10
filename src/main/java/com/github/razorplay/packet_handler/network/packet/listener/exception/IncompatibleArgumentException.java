package com.github.razorplay.packet_handler.network.packet.listener.exception;


import java.util.Objects;

/**
 * Exception thrown when a method parameter cannot be matched with a compatible argument.
 * This typically occurs during dynamic argument assignment in packet handling.
 */
public class IncompatibleArgumentException extends RuntimeException {

    /**
     * Constructs an exception with a message indicating the incompatible parameter and expected type.
     *
     * @param parameterName the name of the parameter that could not be matched
     * @param expectedType  the expected type of the parameter
     * @throws NullPointerException if parameterName or expectedType is null
     */
    public IncompatibleArgumentException(String parameterName, String expectedType) {
        super("No compatible argument found for parameter " +
                Objects.requireNonNull(parameterName, "Parameter name cannot be null") + " of type " +
                Objects.requireNonNull(expectedType, "Expected type cannot be null") + "."
        );
    }
}