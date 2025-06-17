package com.github.razorplay.packet_handler.network.reflection.element.codec.impl;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.network_util.ThrowingBiConsumer;
import com.github.razorplay.packet_handler.network.network_util.ThrowingFunction;
import com.github.razorplay.packet_handler.network.reflection.element.codec.CodecTransform;

import javax.annotation.Nullable;

/**
 * A codec transform that handles nullable types by checking for the {@link Nullable} annotation.
 * Extends {@link CodecTransform} to provide encoding and decoding support for nullable values.
 */
public class NullableCodecTransform extends CodecTransform {

    /**
     * Constructs a NullableCodecTransform that applies to elements annotated with {@link Nullable}.
     * The transformation uses a predicate to check for the presence of the {@link Nullable} annotation.
     */
    public NullableCodecTransform() {
        super(context -> context.getAnnotatedElement().isAnnotationPresent(Nullable.class));
    }

    /**
     * Encodes a potentially nullable value using the provided serializer and encoder.
     * Writes a null indicator followed by the encoded value if non-null.
     *
     * @param <T>     the type of the value to encode
     * @param writer  the serializer to write to
     * @param value   the value to encode, which may be null
     * @param encoder the original encoder function
     * @throws PacketSerializationException if encoding fails
     */
    @Override
    protected <T> void middlewareEncode(PacketDataSerializer writer, T value, ThrowingBiConsumer<PacketDataSerializer, T> encoder) {
        writer.writeNullable(value, encoder);
    }

    /**
     * Decodes a potentially nullable value from the provided serializer.
     * Reads a null indicator and decodes the value if present.
     *
     * @param <T>     the type of the value to decode
     * @param reader  the serializer to read from
     * @param decoder the original decoder function
     * @return the decoded value, which may be null
     * @throws PacketSerializationException if decoding fails
     */
    @Override
    protected <T> T middlewareAfterDecode(PacketDataSerializer reader, ThrowingFunction<PacketDataSerializer, T> decoder) throws PacketSerializationException {
        return reader.readNullable(decoder);
    }
}
