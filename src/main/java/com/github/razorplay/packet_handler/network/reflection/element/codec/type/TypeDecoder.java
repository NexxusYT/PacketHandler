package com.github.razorplay.packet_handler.network.reflection.element.codec.type;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;

/**
 * Functional interface for decoding data from a {@link PacketDataSerializer} into an object of type {@code T}.
 *
 * @param <T> the type of object to decode.
 */
@FunctionalInterface
public interface TypeDecoder<T> {

    /**
     * Decodes data from the given {@link PacketDataSerializer} into an object of type {@code T}.
     *
     * @param reader the data source used to read serialized packet data.
     * @return the decoded object of type {@code T}.
     * @throws PacketSerializationException if decoding fails due to invalid or corrupt data.
     */
    T decode(PacketDataSerializer reader) throws PacketSerializationException;
}
