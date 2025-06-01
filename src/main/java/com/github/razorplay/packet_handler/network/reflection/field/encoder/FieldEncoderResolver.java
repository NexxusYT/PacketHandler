package com.github.razorplay.packet_handler.network.reflection.field.encoder;

import java.lang.reflect.Field;

/**
 * Functional interface responsible for providing a {@link PacketTypeEncoder}
 * for a given {@link Field}.
 *
 * @param <T> the type that the encoder handles.
 */
@FunctionalInterface
public interface FieldEncoderResolver<T> {

    /**
     * Retrieves a {@link PacketTypeEncoder} to be used for the given field.
     *
     * @param field the field for which the encoder is required.
     * @return the corresponding encoder for the field.
     */
    PacketTypeEncoder<T> retrieve(Field field);
}
