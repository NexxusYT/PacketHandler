package com.github.razorplay.packet_handler.network.packet.listener;

import com.github.razorplay.packet_handler.network.IPacket;
import com.github.razorplay.packet_handler.network.packet.annotation.PacketHandler;

/**
 * Marker interface for classes that handle packet events in the packet processing system.
 * Implementations should define methods annotated with {@link PacketHandler} to process
 * specific {@link IPacket} types. These methods are discovered and invoked by the
 * {@link PacketHandlerRegistry} and {@link PacketHandlerContainer} classes.
 *
 * <p>Example usage:
 * <pre>{@code
 * public class MyPacketListener implements PacketListener {
 *      @PacketHandler
 *      public void handle(MyPacket packet, String extraArg) {
 *          Process the packet
 *      }
 * }
 * }
 * </pre>
 *
 * @see PacketHandlerRegistry
 * @see PacketHandlerContainer
 */
public interface PacketListener {
}