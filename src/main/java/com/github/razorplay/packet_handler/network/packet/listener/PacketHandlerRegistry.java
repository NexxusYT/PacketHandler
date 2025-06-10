package com.github.razorplay.packet_handler.network.packet.listener;

import com.github.razorplay.packet_handler.network.IPacket;
import com.github.razorplay.packet_handler.network.packet.annotation.PacketHandler;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry for managing packet handlers, mapping packet types to their respective handler containers.
 * Provides methods to register listeners and invoke handlers for specific packet types.
 */
public final class PacketHandlerRegistry {

    /* Maps packet classes to their corresponding handler containers. */
    private static final Map<Class<? extends IPacket>, PacketHandlerContainer<? extends IPacket>> handlerContainers = new HashMap<>();

    private PacketHandlerRegistry() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Invokes all registered handlers for the given packet's type with the provided arguments.
     *
     * @param packet the packet to handle
     * @param args   additional arguments to pass to handlers
     * @param <T>    the packet type, extending IPacket
     * @throws NullPointerException if the packet is null
     */
    public static <T extends IPacket> void invoke(T packet, Object... args) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        // Retrieve the container for the packet's class
        @SuppressWarnings("unchecked")
        PacketHandlerContainer<T> container = (PacketHandlerContainer<T>) handlerContainers.get(packet.getClass());
        if (container != null) {
            container.invokeHandlers(packet, args);
        }
        // Silently ignore if no container exists, as per original behavior
    }

    /**
     * Registers a listener and its annotated handler methods for processing packets.
     *
     * @param listener the listener containing packet handler methods
     * @throws NullPointerException     if the listener is null
     * @throws IllegalArgumentException if a method's first parameter is not an IPacket subtype
     */
    public static void register(@NotNull PacketListener listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        // Retrieve methods annotated with @PacketHandler
        Class<? extends PacketListener> listenerClass = listener.getClass();
        Method[] handlerMethods = retrieveHandlerMethods(listenerClass);

        // Register each method with its corresponding packet container
        for (Method method : handlerMethods) {
            Class<? extends IPacket> packetClass = getPacketClass(method);
            // Create or get container for the packet class, using the listener
            PacketHandlerContainer<? extends IPacket> container = handlerContainers.computeIfAbsent(packetClass, PacketHandlerContainer::new);
            container.addHandler(listener, method);
        }
    }

    /**
     * Retrieves methods annotated with @PacketHandler from the listener class.
     *
     * @param listenerClass the class to scan for handler methods
     * @return an array of methods annotated with @PacketHandler
     */
    private static Method[] retrieveHandlerMethods(Class<? extends PacketListener> listenerClass) {
        // Filter methods with @PacketHandler annotation
        return Arrays.stream(listenerClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PacketHandler.class))
                .peek(method -> method.setAccessible(true)) // Ensure method accessibility
                .toArray(Method[]::new);
    }

    /**
     * Determines the packet class for a handler method based on its first parameter.
     *
     * @param method the method to analyze
     * @return the packet class (an IPacket subtype)
     * @throws IllegalArgumentException if the method has no parameters or the first parameter is not an IPacket subtype
     */
    private static Class<? extends IPacket> getPacketClass(Method method) {
        method.setAccessible(true);
        if (method.getParameterCount() == 0) {
            throw new IllegalArgumentException("Method " + method.getName() + " has no parameters");
        }
        Parameter firstParameter = method.getParameters()[0];
        if (!firstParameter.getType().isAssignableFrom(IPacket.class)) {
            throw new IllegalArgumentException("Method " + method.getName() + " does not have a parameter that implements IPacket");
        }
        return firstParameter.getType().asSubclass(IPacket.class);
    }
}
