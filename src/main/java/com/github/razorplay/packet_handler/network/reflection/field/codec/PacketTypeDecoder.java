package com.github.razorplay.packet_handler.network.reflection.field.codec;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.reflection.field.GenericTypeAction;

/**
 * Functional interface representing a decoder that deserializes objects
 * of type {@code T} into a {@link PacketDataSerializer}.
 *
 * @param <T> the type of object to decode.
 */
@FunctionalInterface
public interface PacketTypeDecoder<T> extends GenericTypeAction<T> {

    T decode(PacketDataSerializer reader) throws PacketSerializationException;
}
