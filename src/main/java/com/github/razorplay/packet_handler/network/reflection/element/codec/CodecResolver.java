package com.github.razorplay.packet_handler.network.reflection.element.codec;

import com.github.razorplay.packet_handler.network.reflection.element.AnnotatedElementContext;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;

/**
 * Functional interface responsible for resolving a {@link PacketTypeCodec} for a given annotated context.
 *
 * @param <T> the type of object the resolved codec will encode and decode.
 */
@FunctionalInterface
public interface CodecResolver<T> {

    /**
     * Resolves a {@link PacketTypeCodec} for the given annotated element context.
     *
     * @param context the context providing annotations or metadata to guide codec resolution.
     * @return the resolved {@link PacketTypeCodec} for type {@code T}.
     */
    PacketTypeCodec<T> resolveCodec(AnnotatedElementContext context);
}