package com.github.razorplay.packet_handler.network.reflection.field.criteria.impl;

import com.github.razorplay.packet_handler.network.reflection.field.criteria.PrioritizedFieldEncoder;
import com.github.razorplay.packet_handler.network.reflection.field.encoder.PacketTypeEncoder;

/**
 * A convenience encoder that applies the provided {@link PacketTypeEncoder} to fields
 * whose type is assignable to the specified class.
 *
 * @param <T> the type handled by the encoder.
 */
public final class TypeMatchEncoder<T> extends PrioritizedFieldEncoder<T> {

    /**
     * Constructs an encoder that applies to any field whose type is assignable
     * to the given class.
     *
     * @param typeClass   the target type that fields must match or extend.
     * @param typeEncoder the encoder to use for matching fields.
     */
    public TypeMatchEncoder(Class<T> typeClass, PacketTypeEncoder<T> typeEncoder) {
        super((field, unwrappedType, object) -> typeClass.isAssignableFrom(unwrappedType), field -> typeEncoder);
    }
}
