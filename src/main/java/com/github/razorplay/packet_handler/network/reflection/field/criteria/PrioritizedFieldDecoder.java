package com.github.razorplay.packet_handler.network.reflection.field.criteria;

import com.github.razorplay.packet_handler.network.reflection.field.FieldPredicate;
import com.github.razorplay.packet_handler.network.reflection.field.codec.PacketTypeDecoder;
import com.github.razorplay.packet_handler.network.reflection.field.codec.resolver.FieldDecoderResolver;
import com.github.razorplay.packet_handler.network.reflection.field.codec.resolver.FieldEncoderResolver;
import lombok.Getter;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;

/**
 * A composite encoder that associates a {@link FieldPredicate} with a corresponding
 * {@link FieldEncoderResolver} and an optional priority value.
 * <p>
 * This allows conditional application of encoders based on field characteristics,
 * with priority used to resolve conflicts when multiple matchers apply.
 *
 * @param <T> the type of object this encoder will handle.
 */
public abstract class PrioritizedFieldDecoder<T> implements FieldPredicate, FieldDecoderResolver<T> {

    @Getter
    private final int priority;
    private final FieldPredicate fieldMatch;
    private final FieldDecoderResolver<T> fieldDeserializerGetter;

    /**
     * Constructs a prioritized decoder with default priority {@code 0}.
     *
     * @param fieldMatch              the predicate used to determine applicability.
     * @param fieldDeserializerGetter the decoder provider for matching fields.
     */
    public PrioritizedFieldDecoder(FieldPredicate fieldMatch, FieldDecoderResolver<T> fieldDeserializerGetter) {
        this(0, fieldMatch, fieldDeserializerGetter);
    }

    /**
     * Constructs a prioritized decoder with the given priority.
     *
     * @param priority                the priority value (lower = higher priority).
     * @param fieldMatch              the predicate used to determine applicability.
     * @param fieldDeserializerGetter the decoder provider for matching fields.
     */
    public PrioritizedFieldDecoder(int priority, FieldPredicate fieldMatch, FieldDecoderResolver<T> fieldDeserializerGetter) {
        this.priority = priority;
        this.fieldMatch = fieldMatch;
        this.fieldDeserializerGetter = fieldDeserializerGetter;
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
    public boolean matches(AnnotatedElement field, Class<?> unwrappedType, @Nullable Object value) {
        return fieldMatch.matches(field, unwrappedType, value);
    }

    /**
     * Retrieves the decoder to use for a matching field.
     *
     * @param field the field to be decoded.
     * @return the corresponding {@link PacketTypeDecoder} for the field.
     */
    @Override
    public PacketTypeDecoder<T> get(AnnotatedElement field) {
        return fieldDeserializerGetter.get(field);
    }
}
