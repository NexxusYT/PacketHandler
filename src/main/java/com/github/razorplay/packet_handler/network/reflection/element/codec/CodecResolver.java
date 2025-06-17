package com.github.razorplay.packet_handler.network.reflection.element.codec;

import com.github.razorplay.packet_handler.network.reflection.element.AnnotatedElementContext;
import com.github.razorplay.packet_handler.network.reflection.element.codec.impl.NullableCodecTransform;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;

/**
 * Functional interface responsible for resolving a {@link PacketTypeCodec} for a given annotated context.
 *
 * @param <T> the type of object the resolved codec will encode and decode.
 */
@FunctionalInterface
public interface CodecResolver {

    CodecTransform[] DEFAULTED_TRANSFORMS = {
            new NullableCodecTransform()
    };

    /**
     * Resolves a {@link PacketTypeCodec} for the given annotated element context.
     *
     * @param context the context providing annotations or metadata to guide codec resolution.
     * @return the resolved {@link PacketTypeCodec} for type {@code T}.
     */
    PacketTypeCodec<?> resolveCodec(AnnotatedElementContext context);

    /**
     * Resolves a transformed codec by applying a series of codec transformations to the base codec.
     * The transformations are applied based on the provided annotated element context.
     *
     * @param context the annotated element context used to resolve and transform the codec
     * @return the transformed {@link PacketTypeCodec} after applying all relevant transformations
     */
    default PacketTypeCodec<?> resolveTransformedCodec(AnnotatedElementContext context) {
        // Resolve the base codec for the given context
        PacketTypeCodec<?> codec = resolveCodec(context);

        // Apply each transformation in sequence
        for (CodecTransform transform : getTransforms()) {
            codec = transform.applyCodec(context, codec);
        }

        return codec;
    }

    /**
     * Retrieves the array of codec transformations to be applied.<br>
     * By default, returns the default transformations defined in {@link CodecResolver}.
     *
     * @return an array of {@link CodecTransform} objects to be applied to codecs
     */
    default CodecTransform[] getTransforms() {
        // Return the default set of transformations
        return CodecResolver.DEFAULTED_TRANSFORMS;
    }
}