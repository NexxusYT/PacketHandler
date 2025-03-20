package com.github.razorplay.packet_handler.network.packet;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.IPacket;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import lombok.NoArgsConstructor;

/**
 * A basic implementation of IPacket that serves as both a minimal example and a utility packet.
 * This packet contains no data and performs no operations, making it useful for simple signaling
 * or as a template for creating new packet types.
 *
 * <p>Example usage of creating a new packet based on this template:
 * <pre>
 * {@code
 * @NoArgsConstructor
 * public class MyNewPacket implements IPacket {
 *     private String someData;
 *
 *     @Override
 *     public void read(PacketDataSerializer serializer) throws PacketSerializationException {
 *         this.someData = serializer.readString();
 *     }
 *
 *     @Override
 *     public void write(PacketDataSerializer serializer) throws PacketSerializationException {
 *         serializer.writeString(this.someData);
 *     }
 *
 *     @Override
 *     public String getPacketId() {
 *         return "MyNewPacket";
 *     }
 * }
 * }</pre>
 *
 * @see IPacket
 */
@NoArgsConstructor
public class EmptyPacket implements IPacket {

    @Override
    public void read(PacketDataSerializer serializer) throws PacketSerializationException {
        // Empty implementation - no data to read
    }

    @Override
    public void write(PacketDataSerializer serializer) throws PacketSerializationException {
        // Empty implementation - no data to write
    }

    /**
     * Returns the unique identifier for this packet type.
     *
     * @return The string "EmptyPacket" as this packet's identifier
     */
    @Override
    public String getPacketId() {
        return "EmptyPacket";
    }
}
