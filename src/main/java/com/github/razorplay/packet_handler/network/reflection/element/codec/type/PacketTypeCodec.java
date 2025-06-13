package com.github.razorplay.packet_handler.network.reflection.element.codec.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a codec for a specific packet type, including both an encoder and a decoder
 * for a given data type.
 *
 * <pre><code>
 *         // Example
 *         new PacketTypeCodec<>(
 *              PacketDataSerializer::writeString,
 *              PacketDataSerializer::readString
 *         );
 * </code></pre>
 *
 * @param <T> the type of data to be encoded and decoded.
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class PacketTypeCodec<T> {

    /**
     * Encoder for the {@code T} type, used to serialize the object into a transferable format.
     */
    private final TypeEncoder<T> writer;

    /**
     * Decoder for the {@code T} type, used to deserialize data back into an object instance.
     */
    private final TypeDecoder<T> reader;
}
