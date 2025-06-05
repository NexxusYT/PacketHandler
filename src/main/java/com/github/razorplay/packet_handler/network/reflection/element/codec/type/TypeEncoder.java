package com.github.razorplay.packet_handler.network.reflection.element.codec.type;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;

/**
 * Functional interface for encoding an object of type {@code T} into a {@link PacketDataSerializer}.
 *
 * @param <T> the type of object to encode.
 */
@FunctionalInterface
public interface TypeEncoder<T> {

    /**
     * Encodes the given object into the provided {@link PacketDataSerializer}.
     *
     * @param writer the destination used to write the serialized data.
     * @param value  the object to encode.
     * @throws PacketSerializationException if encoding fails due to serialization issues.
     */
    void encode(PacketDataSerializer writer, T value) throws PacketSerializationException;
}
