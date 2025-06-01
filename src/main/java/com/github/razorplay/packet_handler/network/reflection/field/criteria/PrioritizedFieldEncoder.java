package com.github.razorplay.packet_handler.network.reflection.field.criteria;

import com.github.razorplay.packet_handler.network.reflection.field.encoder.FieldEncoderResolver;
import com.github.razorplay.packet_handler.network.reflection.field.encoder.PacketTypeEncoder;
import lombok.Getter;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * A composite encoder that associates a {@link FieldPredicate} with a corresponding
 * {@link FieldEncoderResolver} and an optional priority value.
 * <p>
 * This allows conditional application of encoders based on field characteristics,
 * with priority used to resolve conflicts when multiple matchers apply.
 *
 * @param <T> the type of object this encoder will handle.
 */
public abstract class PrioritizedFieldEncoder<T> implements FieldPredicate, FieldEncoderResolver<T> {

    @Getter
    private final int priority;
    private final FieldPredicate fieldMatch;
    private final FieldEncoderResolver<T> fieldSerializerGetter;

    /**
     * Constructs a prioritized encoder with default priority {@code 0}.
     *
     * @param fieldMatch            the predicate used to determine applicability.
     * @param fieldSerializerGetter the encoder provider for matching fields.
     */
    public PrioritizedFieldEncoder(FieldPredicate fieldMatch, FieldEncoderResolver<T> fieldSerializerGetter) {
        this(0, fieldMatch, fieldSerializerGetter);
    }

    /**
     * Constructs a prioritized encoder with the given priority.
     *
     * @param priority              the priority value (lower = higher priority).
     * @param fieldMatch            the predicate used to determine applicability.
     * @param fieldSerializerGetter the encoder provider for matching fields.
     */
    public PrioritizedFieldEncoder(int priority, FieldPredicate fieldMatch, FieldEncoderResolver<T> fieldSerializerGetter) {
        this.priority = priority;
        this.fieldMatch = fieldMatch;
        this.fieldSerializerGetter = fieldSerializerGetter;
    }

    /**
     * Evaluates whether the given field matches the defined criteria.
     *
     * @param field         the field to evaluate.
     * @param unwrappedType the unboxed type of the field.
     * @param value         the runtime value of the field (nullable).
     * @return {@code true} if the field matches, {@code false} otherwise.
     */
    @Override
    public boolean matches(Field field, Class<?> unwrappedType, @Nullable Object value) {
        return fieldMatch.matches(field, unwrappedType, value);
    }

    /**
     * Retrieves the encoder to use for a matching field.
     *
     * @param field the field to be encoded.
     * @return the corresponding {@link PacketTypeEncoder} for the field.
     */
    @Override
    public PacketTypeEncoder<T> retrieve(Field field) {
        return fieldSerializerGetter.retrieve(field);
    }
}
