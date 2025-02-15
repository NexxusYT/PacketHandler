package com.github.razorplay.packet_handler.network;

import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.exceptions.PacketSerializationException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

/**
 * Interface defining the contract for network packets in the system.
 * Classes implementing this interface must provide methods for serialization,
 * deserialization, and packet identification.
 */
public interface IPacket {

    /**
     * Deserializes packet data from a byte array input buffer.
     * This method should read the packet's data in the same order it was written.
     *
     * @param buf The input buffer containing the packet data to be read
     * @throws PacketSerializationException if there's an error during deserialization
     */
    void read(final ByteArrayDataInput buf) throws PacketSerializationException;

    /**
     * Serializes packet data into a byte array output buffer.
     * This method should write the packet's data in a consistent order that matches the read method.
     *
     * @param buf The output buffer where the packet data will be written
     * @throws PacketSerializationException if there's an error during serialization
     */
    void write(final ByteArrayDataOutput buf) throws PacketSerializationException;

    /**
     * Returns the unique identifier for this packet type.
     * The ID should be unique across all packet implementations to ensure proper routing.
     *
     * @return A string representing the unique identifier of the packet
     */
    String getPacketId();
}