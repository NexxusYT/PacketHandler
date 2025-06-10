package com.github.razorplay.packet_handler.network.packet.listener;

import com.github.razorplay.packet_handler.network.IPacket;
import com.github.razorplay.packet_handler.network.packet.listener.exception.IncompatibleArgumentException;
import com.github.razorplay.packet_handler.network.packet.listener.exception.InsufficientArgumentException;
import com.github.razorplay.packet_handler.network.packet.listener.exception.ListenerNotDeclaredException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Manages packet handlers for a specific packet type, associating methods with listeners
 * and invoking them with appropriate arguments when packets are received.
 *
 * @param <T> the type of packet, extending IPacket
 */
@RequiredArgsConstructor
public final class PacketHandlerContainer<T extends IPacket> {

    private final List<Method> methods = new ArrayList<>();

    // Map of non-static methods to their corresponding listeners.
    private final Map<Method, PacketListener> methodListeners = new HashMap<>();

    // The packet class this container handles.
    @Getter
    private final Class<T> packetClass;

    /**
     * Populates method arguments by matching parameter types with provided arguments.
     *
     * @param method the method to populate arguments for
     * @param packet the packet instance (first argument)
     * @param args   additional arguments to match against method parameters
     * @return an array of arguments for method invocation
     * @throws IllegalArgumentException if insufficient arguments are provided or types don't match
     */
    private static Object[] populateArgs(Method method, Object packet, Object... args) {
        // Initialize array to hold method arguments
        Object[] argInstances = new Object[method.getParameterCount()];
        argInstances[0] = packet; // Set first argument as packet

        // Check if enough arguments are provided
        if (argInstances.length > args.length + 1) {
            throw new InsufficientArgumentException(method.getName(), argInstances.length - 1, args.length);
        }

        // Track used arguments to prevent reuse
        boolean[] used = new boolean[args.length];

        // Match remaining parameters with available arguments
        for (int i = 1; i < argInstances.length; i++) {
            Class<?> requiredType = method.getParameters()[i].getType();
            boolean assigned = false;

            // Find a compatible, unused argument
            for (int j = 0; j < args.length; j++) {
                if (!used[j] && args[j] != null && requiredType.isAssignableFrom(args[j].getClass())) {
                    argInstances[i] = args[j];
                    used[j] = true;
                    assigned = true;
                    break;
                }
            }

            // Throw if no compatible argument is found
            if (!assigned) {
                throw new IncompatibleArgumentException(method.getParameters()[i].getName(), requiredType.getSimpleName());
            }
        }
        return argInstances;
    }

    /**
     * Invokes a handler method with the given packet and arguments.
     *
     * @param listener the listener instance (null for static methods)
     * @param method   the method to invoke
     * @param packet   the packet to pass as the first argument
     * @param args     additional arguments to match method parameters
     * @param <T>      the packet type
     */
    public static <T extends IPacket> void invokeHandler(PacketListener listener, Method method, T packet, Object... args) {
        try {
            Object[] argInstances = populateArgs(method, packet, args);
            method.setAccessible(true);
            method.invoke(listener, argInstances);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Registers a handler method and its associated listener.
     *
     * @param listener the listener instance containing the method
     * @param method   the method to register as a handler
     * @throws IllegalArgumentException if the method is invalid or incompatible
     */
    public void addHandler(PacketListener listener, Method method) {
        // Validate listener and method compatibility
        Objects.requireNonNull(method, "Method cannot be null");

        if (listener != null && !method.getDeclaringClass().isInstance(listener)) {
            throw new ListenerNotDeclaredException(method.getName(), listener.getClass().getSimpleName());
        }
        if (method.isSynthetic()) {
            throw new IllegalArgumentException("Synthetic methods are not allowed: " + method.getName());
        }

        if (!packetClass.isAssignableFrom(method.getParameterTypes()[0])) {
            throw new IllegalArgumentException("Method " + method.getName() +
                    " first parameter must be of type " + packetClass.getSimpleName());
        }

        // Register the method and listener
        this.methods.add(method);
        if (listener != null && !Modifier.isStatic(method.getModifiers())) {
            this.methodListeners.put(method, listener);
        }
    }

    /**
     * Invokes all registered handlers with the given packet and arguments.
     *
     * @param packet the packet to pass to handlers
     * @param args   additional arguments to match method parameters
     */
    public void invokeHandlers(T packet, Object... args) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        for (Method method : this.methods) {
            PacketListener listener = methodListeners.getOrDefault(method, null);
            invokeHandler(listener, method, packet, args);
        }
    }
}
