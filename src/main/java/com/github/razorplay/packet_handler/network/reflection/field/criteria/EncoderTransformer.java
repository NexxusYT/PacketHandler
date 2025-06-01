package com.github.razorplay.packet_handler.network.reflection.field.criteria;

import com.github.razorplay.packet_handler.network.reflection.field.encoder.PacketTypeEncoder;

/**
 * Functional interface representing a transformation from one {@link PacketTypeEncoder} to another,
 * potentially changing the encoded output type based on the input object.
 * <p>
 * This is useful when the encoding behavior needs to be dynamically altered or decorated
 * depending on the context of the input object, such as encoding only specific aspects
 * or adapting the output format.
 *
 * @param <T> the input object type that is being encoded.
 * @param <V> the output type that the resulting encoder will produce.
 */
@FunctionalInterface
public interface EncoderTransformer<T, V> {

    /**
     * Transforms a given encoder into a new one for type {@code V},
     * optionally using the parameters to influence the transformation.
     *
     * @param object  the instance of {@code T} used as context for the transformation.
     * @param encoder the original encoder capable of encoding type {@code T}.
     * @return a new {@link PacketTypeEncoder} capable of encoding type {@code V}.
     */
    PacketTypeEncoder<V> transform(T object, PacketTypeEncoder<T> encoder);
}
