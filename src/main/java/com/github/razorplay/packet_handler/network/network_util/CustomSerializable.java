package com.github.razorplay.packet_handler.network.network_util;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;

/**
 * Interface for custom objects that can be serialized and deserialized using PacketDataSerializer.
 */
public interface CustomSerializable {
    void serialize(PacketDataSerializer serializer);
    void deserialize(PacketDataSerializer serializer) throws PacketSerializationException;
}