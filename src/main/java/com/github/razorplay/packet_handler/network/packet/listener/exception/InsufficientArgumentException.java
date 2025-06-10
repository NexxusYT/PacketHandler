package com.github.razorplay.packet_handler.network.packet.listener.exception;

/**
 * Exception thrown when a method receives fewer arguments than expected during packet handling.
 */
public class InsufficientArgumentException extends IllegalArgumentException {

    /**
     * Constructs an exception with a message indicating the method and argument mismatch.
     *
     * @param methodName the name of the method with insufficient arguments
     * @param expected   the number of arguments expected
     * @param actual     the number of arguments provided
     * @throws NullPointerException if methodName is null
     */
    public InsufficientArgumentException(String methodName, int expected, int actual) {
        super("Insufficient arguments for method " + methodName + ": expected " + expected + ", got " + actual);
    }
}
