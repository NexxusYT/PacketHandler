package com.github.razorplay.packet_handler.network.packet.listener.exception;

import java.util.Objects;

/**
 * Exception thrown when a method is not declared in the specified listener class.
 * This typically occurs during packet handler registration when a method is not found in the listener's class.
 */
public class ListenerNotDeclaredException extends IllegalArgumentException {

    /**
     * Constructs an exception with a message indicating the method and listener class mismatch.
     *
     * @param methodName    the name of the method not declared in the listener class
     * @param listenerClass the name of the listener class
     * @throws NullPointerException if methodName or listenerClass is null
     */
    public ListenerNotDeclaredException(String methodName, String listenerClass) {
        super("Listener method " +
                Objects.requireNonNull(methodName, "Method name cannot be null") + " not declared in class " +
                Objects.requireNonNull(listenerClass, "Listener class cannot be null")
        );
    }
}
