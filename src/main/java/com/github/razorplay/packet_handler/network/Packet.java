package com.github.razorplay.packet_handler.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation used to mark classes that represent network packets in the system.
 * Classes annotated with @Packet must implement the IPacket interface.
 * This annotation is used during packet scanning and registration process.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * @Packet
 * public class MyCustomPacket implements IPacket {
 *     // Packet implementation
 * }
 * }</pre>
 *
 * @see IPacket
 * @see PacketTCP#scanAndRegisterPackets(String)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Packet {
}