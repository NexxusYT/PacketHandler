package com.github.razorplay.packet_handler.network.reflection.element.codec.type;

import com.github.razorplay.packet_handler.network.reflection.element.AnnotatedElementContext;
import com.github.razorplay.packet_handler.network.reflection.element.ElementPredicate;

/**
 * An interface for transforming packet codecs, encoders, and decoders based on annotated element contexts.<br>
 * Extends {@link ElementPredicate} to allow matching logic for transformation applicability.
 */
public interface TypeTransform extends ElementPredicate {

    /**
     * Applies a transformation to a packet codec if the context matches the predicate.
     *
     * @param <T>     the type of the codec
     * @param context the annotated element context to evaluate
     * @param codec   the original codec to transform
     * @return a transformed {@link PacketTypeCodec} or the original codec if no transformation applies
     */
    <T> PacketTypeCodec<T> applyCodec(AnnotatedElementContext context, PacketTypeCodec<T> codec);

    /**
     * Applies a transformation to a type encoder.
     *
     * @param <T>     the type of the value to encode
     * @param encoder the original encoder to transform
     * @return a transformed {@link TypeEncoder} for encoding values
     */
    <T> TypeEncoder<T> applyEncoder(TypeEncoder<T> encoder);

    /**
     * Applies a transformation to a type decoder.
     *
     * @param <T>     the type of the value to decode
     * @param decoder the original decoder to transform
     * @return a transformed {@link TypeDecoder} for decoding values
     */
    <T> TypeDecoder<T> applyDecoder(TypeDecoder<T> decoder);
}
