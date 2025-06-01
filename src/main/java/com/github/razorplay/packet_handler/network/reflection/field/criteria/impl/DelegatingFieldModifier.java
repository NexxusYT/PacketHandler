package com.github.razorplay.packet_handler.network.reflection.field.criteria.impl;

import com.github.razorplay.packet_handler.network.reflection.field.criteria.EncoderTransformer;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.FieldPredicate;
import com.github.razorplay.packet_handler.network.reflection.field.encoder.PacketTypeEncoder;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * A transformer that conditionally applies an {@link EncoderTransformer}
 * if a {@link FieldPredicate} matches the target field.
 *
 * @param <T> the input type expected by the original encoder.
 * @param <U> the resulting output type after transformation.
 */
public class DelegatingFieldModifier<T, U> implements EncoderTransformer<T, U>, FieldPredicate {

    private final FieldPredicate predicate;
    private final EncoderTransformer<T, U> transformer;

    /**
     * Constructs a conditional encoder transformer.
     *
     * @param predicate   the predicate that determines whether this transformer applies.
     * @param transformer the transformer to apply if the predicate matches.
     */
    public DelegatingFieldModifier(FieldPredicate predicate, EncoderTransformer<T, U> transformer) {
        this.predicate = predicate;
        this.transformer = transformer;
    }

    /**
     * Evaluates whether this transformer applies to the given field.
     *
     * @param field         the field being encoded.
     * @param unwrappedType the unboxed field type.
     * @param value         the runtime field value (nullable).
     * @return true if this transformer should be applied.
     */
    @Override
    public boolean matches(Field field, Class<?> unwrappedType, @Nullable Object value) {
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
    public PacketTypeEncoder<U> transform(T object, PacketTypeEncoder<T> typeEncoder) {
        return transformer.transform(object, typeEncoder);
    }
}
