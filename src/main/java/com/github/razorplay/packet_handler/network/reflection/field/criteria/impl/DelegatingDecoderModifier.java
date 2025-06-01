package com.github.razorplay.packet_handler.network.reflection.field.criteria.impl;

import com.github.razorplay.packet_handler.network.reflection.field.FieldPredicate;
import com.github.razorplay.packet_handler.network.reflection.field.codec.GenericTransformer;
import com.github.razorplay.packet_handler.network.reflection.field.codec.PacketTypeDecoder;
import lombok.AllArgsConstructor;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

@AllArgsConstructor
public class DelegatingDecoderModifier<T, U> implements GenericTransformer<T, U,
        PacketTypeDecoder<T>,
        PacketTypeDecoder<U>>, FieldPredicate {

    private final FieldPredicate predicate;
    private final GenericTransformer<T, U,
            PacketTypeDecoder<T>,
            PacketTypeDecoder<U>> transformer;

    /**
     * Evaluates whether this transformer applies to the given field.
     *
     * @param field         the field being encoded.
     * @param unwrappedType the unboxed field type.
     * @param value         the runtime field value (nullable).
     * @return true if this transformer should be applied.
     */
    @Override
    public boolean matches(AnnotatedElement field, Class<?> unwrappedType, @Nullable Object value) {
        return predicate.matches(field, unwrappedType, value);
    }

    /**
     * Applies the transformation logic to the encoder.
     *
     * @param object      the object instance being encoded.
     * @param typeEncoder the original encoder.
     * @return the transformed encoder.
     */
    @Override
    public PacketTypeDecoder<U> transform(T object, PacketTypeDecoder<T> typeEncoder) {
        return transformer.transform(object, typeEncoder);
    }
}
