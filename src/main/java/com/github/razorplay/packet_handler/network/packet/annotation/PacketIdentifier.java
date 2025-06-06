package com.github.razorplay.packet_handler.network.packet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to associate a unique identifier with a packet class.
 *
 * <p>This is typically used in serialization frameworks to register and look up
 * packet types by their identifiers during encoding or decoding.</p>
 *
 * <pre>{@code
 * @PacketIdentifier(id = "example:my_packet")
 * public class MyPacket implements SimplePacket {
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PacketIdentifier {

    /**
     * The unique string identifier for the packet.
     *
     * @return the packet ID (e.g., "namespace:packet_name").
     */
    String id();
}