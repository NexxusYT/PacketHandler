package com.github.razorplay.packet_handler.network.reflection.field.encoder;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;

/**
 * Functional interface representing an encoder that serializes objects
 * of type {@code T} into a {@link PacketDataSerializer}.
 *
 * @param <T> the type of object to encode.
 */
@FunctionalInterface
public interface PacketTypeEncoder<T> {

    /**
     * Encodes the given value using the provided {@link PacketDataSerializer}.
     *
     * @param writer the serializer to write the encoded data.
     * @param value  the value to encode.
     * @throws PacketSerializationException if encoding fails.
     */
    void encode(PacketDataSerializer writer, T value) throws PacketSerializationException;
}
