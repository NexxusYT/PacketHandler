package com.github.razorplay.packet_handler.network.packet;

import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.exceptions.PacketSerializationException;
import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.network.IPacket;
import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.network.Packet;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.NoArgsConstructor;


/**
 * A basic implementation of IPacket that serves as both a minimal example and a utility packet.
 * This packet contains no data and performs no operations, making it useful for simple signaling
 * or as a template for creating new packet types.
 *
 * <p>Example usage of creating a new packet based on this template:
 * <pre>
 * {@code
 * @Packet
 * @NoArgsConstructor
 * public class MyNewPacket implements IPacket {
 *     private String someData;
 *
 *     @Override
 *     public void read(ByteArrayDataInput buf) throws PacketSerializationException {
 *         this.someData = buf.readUTF();
 *     }
 *
 *     @Override
 *     public void write(ByteArrayDataOutput buf) throws PacketSerializationException {
 *         buf.writeUTF(this.someData);
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
 * @see Packet
 */
@Packet
@NoArgsConstructor
public class EmptyPacket implements IPacket {

    /**
     * Reads packet data from the input buffer.
     * This implementation is empty as this packet contains no data.
     *
     * @param buf The input buffer (unused in this implementation)
     * @throws PacketSerializationException if there's an error during deserialization
     */
    @Override
    public void read(ByteArrayDataInput buf) throws PacketSerializationException {
        // Empty implementation - no data to read
    }

    /**
     * Writes packet data to the output buffer.
     * This implementation is empty as this packet contains no data.
     *
     * @param buf The output buffer (unused in this implementation)
     * @throws PacketSerializationException if there's an error during serialization
     */
    @Override
    public void write(ByteArrayDataOutput buf) throws PacketSerializationException {
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
