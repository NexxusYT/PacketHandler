package com.github.razorplay.packet_handler.network.reflection.element.codec;

import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;

/**
 * A {@link PrioritizedCodecResolver} implementation that resolves a codec based on type matching.
 *
 * <p>If the context's unwrapped type is assignable from the given {@code type}, the associated
 * codec will be used for encoding/decoding.</p>
 *
 * @param <T> the type of object the codec handles.
 */
public final class TypeMatchCodecResolver<T> extends PrioritizedCodecResolver {

    /**
     * Constructs a {@code TypeMatchCodecResolver} with the given type and codec.
     *
     * <p>This resolver will match any context whose unwrapped type is assignable from {@code type}.</p>
     *
     * @param type  the base type to match against the context's unwrapped type.
     * @param codec the codec to use when the type matches.
     */
    public TypeMatchCodecResolver(Class<T> type, PacketTypeCodec<T> codec) {
        super(
                context -> codec, // Static codec resolve function
                context -> type.isAssignableFrom(context.getUnwrappedType())
        );
    }
}