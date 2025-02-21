package com.github.razorplay.packet_handler.network;


import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;

/**
 * Interface defining the contract for network packets in the system.
 * Classes implementing this interface must provide methods for serialization,
 * deserialization, and packet identification.
 */
public interface IPacket {

    /**
     * Deserializes packet data from a serializer that reads from a byte array input buffer.
     * This method should read the packet's data in the same order it was written during serialization.
     * Implementations must use the provided {@link PacketDataSerializer} to read primitive types,
     * collections, maps, or custom objects, ensuring consistency with the corresponding {@link #write(PacketDataSerializer)} method.
     *
     * @param serializer The serializer used to read packet data from the input buffer.
     *                   It provides methods to read various data types, such as primitives, lists, maps, and custom serializable objects.
     * @throws PacketSerializationException if there is an error during deserialization, such as invalid data format,
     *                                      unexpected end of input, or failure to read a specific data type.
     */
    void read(final PacketDataSerializer serializer) throws PacketSerializationException;

    /**
     * Serializes packet data into a serializer that writes to a byte array output buffer.
     * This method should write the packet's data in a consistent order that matches the corresponding {@link #read(PacketDataSerializer)} method.
     * Implementations must use the provided {@link PacketDataSerializer} to write primitive types,
     * collections, maps, or custom objects, ensuring proper serialization for network transmission or storage.
     *
     * @param serializer The serializer used to write packet data to the output buffer.
     *                   It provides methods to write various data types, such as primitives, lists, maps, and custom serializable objects.
     * @throws PacketSerializationException if there is an error during serialization, such as failure to write a specific data type
     *                                      or issues with the underlying output buffer.
     */
    void write(final PacketDataSerializer serializer) throws PacketSerializationException;

    /**
     * Returns the unique identifier for this packet type.
     * The ID should be unique across all packet implementations to ensure proper routing.
     *
     * @return A string representing the unique identifier of the packet
     */
    String getPacketId();
}