package com.github.razorplay.packet_handler.network.reflection.field.criteria.impl;

import com.github.razorplay.packet_handler.network.reflection.field.codec.PacketTypeDecoder;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.PrioritizedFieldDecoder;

/**
 * A convenience decoder that applies the provided {@link PrioritizedFieldDecoder} to fields
 * whose type is assignable to the specified class.
 *
 * @param <T> the type handled by the decoder.
 */
public final class TypeMatchDecoder<T> extends PrioritizedFieldDecoder<T> {

    /**
     * Constructs a decoder that applies to any field whose type is assignable
     * to the given class.
     *
     * @param typeClass   the target type that fields must match or extend.
     * @param typeDecoder the decoder to use for matching fields.
     */
    public TypeMatchDecoder(Class<T> typeClass, PacketTypeDecoder<T> typeDecoder) {
        super((field, unwrappedType, object) -> typeClass.isAssignableFrom(unwrappedType), field -> typeDecoder);
    }
}
