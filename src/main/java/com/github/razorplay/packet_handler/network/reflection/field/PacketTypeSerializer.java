package com.github.razorplay.packet_handler.network.reflection.field;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.reflection.ReflectionUtil;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.PrioritizedFieldEncoder;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.impl.DelegatingFieldModifier;
import com.github.razorplay.packet_handler.network.reflection.field.encoder.PacketTypeEncoder;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PacketTypeSerializer {

    private static final Map<Field, PacketTypeEncoder<?>> CACHED_ENCODERS = new HashMap<>();

    private static final PrioritizedFieldEncoder<?>[] SORTED_ENCODERS;
    private static final PrioritizedFieldEncoder<?>[] ENCODER_LIST = {
            BuiltInFieldEncoders.STRING_ENCODER,
            BuiltInFieldEncoders.INTEGER_ENCODER
    };

    // List of predefined field encoder transformers (e.g., for nullable support).
    private static final DelegatingFieldModifier<?, ?>[] TRANSFORMERS = {BuiltInFieldEncoders.NULLABLE_TRANSFORMER};

    static {
        SORTED_ENCODERS = Arrays.stream(ENCODER_LIST)
                // Create a sorted encoders using the priority property
                .sorted(Comparator.comparingInt(PrioritizedFieldEncoder::getPriority))
                .toArray(PrioritizedFieldEncoder<?>[]::new);
    }

    /**
     * Retrieves the appropriate {@link PacketTypeEncoder} for a given field and its value.
     * The encoder is selected by checking the prioritized list of encoders to find the first
     * that matches the field's unwrapped type and the value.
     *
     * @param field  the {@link Field} to be encoded
     * @param object the value of the field; can be {@code null}
     * @return the matching {@link PacketTypeEncoder} for the field
     * @throws PacketSerializationException if no suitable encoder is found
     */
    public static PacketTypeEncoder<?> findEncoder(Field field, @Nullable Object object) throws PacketSerializationException {
        Class<?> unwrappedType = ReflectionUtil.unwrapBoxedType(field.getType());

        for (PrioritizedFieldEncoder<?> encoder : PacketTypeSerializer.SORTED_ENCODERS) {
            if (encoder.matches(field, unwrappedType, object)) {
                return encoder.retrieve(field);
            }
        }

        throw new PacketSerializationException("No serializer found for field " + field.getName());
    }

    /**
     * Applies a given {@link DelegatingFieldModifier} to transform the provided encoder.
     *
     * <p>If the modifier matches, attempts to cast the value and encoder to the expected types,
     * applies the transformation, and returns the modified encoder. If a {@link ClassCastException}
     * occurs, logs the exception and returns the original encoder.</p>
     *
     * @param criteriaModifier the field modifier/transformer to apply
     * @param field            the field being encoded
     * @param object           the value of the field; may be null
     * @param unwrappedType    the unwrapped type of the field (e.g., primitive instead of wrapper)
     * @param typeEncoder      the original encoder to potentially transform
     * @param <T>              the input object type for the transformer
     * @param <U>              the output type of the transformed encoder
     * @return the transformed encoder if criteria matches and cast succeeds; otherwise, the original encoder
     */
    @SuppressWarnings("unchecked")
    private static <T, U> PacketTypeEncoder<?> applyModifierIfMatches(
            DelegatingFieldModifier<T, U> criteriaModifier,
            Field field,
            Object object,
            Class<?> unwrappedType,
            PacketTypeEncoder<?> typeEncoder
    ) {

        if (criteriaModifier.matches(field, unwrappedType, object)) {
            try {
                return criteriaModifier.transform((T) object, (PacketTypeEncoder<T>) typeEncoder);
            } catch (ClassCastException exception) {
                // Consider proper logging instead of printStackTrace
                exception.printStackTrace(System.out);
            }
        }

        return typeEncoder;
    }

    /**
     * Applies all registered {@link DelegatingFieldModifier}s (transformers) that match the given field and value
     * to the provided encoder, returning the potentially modified encoder.
     *
     * @param field       the field to be encoded
     * @param object      the value of the field; may be {@code null}
     * @param typeEncoder the original encoder for the field
     * @return the resulting encoder after applying all matching modifiers
     */
    public static PacketTypeEncoder<?> applyTransformers(Field field, Object object, PacketTypeEncoder<?> typeEncoder) {
        PacketTypeEncoder<?> currentEncoder = typeEncoder;
        Class<?> unwrappedType = ReflectionUtil.unwrapBoxedType(field.getType());

        for (DelegatingFieldModifier<?, ?> modifier : PacketTypeSerializer.TRANSFORMERS) {
            if (modifier.matches(field, unwrappedType, object)) {
                currentEncoder = applyModifierIfMatches(modifier, field, object, unwrappedType, currentEncoder);
            }
        }

        return currentEncoder;
    }

    /**
     * Retrieves the appropriate {@link PacketTypeEncoder} for the specified field and value,
     * then applies any matching modifiers/transformers to the encoder before returning it.
     *
     * @param field  the field to be encoded
     * @param object the value of the field; may be {@code null}
     * @return the encoder for the field after applying all matching modifiers
     * @throws PacketSerializationException if no suitable encoder is found for the field
     */
    public static PacketTypeEncoder<?> getEncoderWithTransformers(Field field, Object object) throws PacketSerializationException {
        return PacketTypeSerializer.applyTransformers(field, object, PacketTypeSerializer.findEncoder(field, object));
    }

    /**
     * Retrieves the cached {@link PacketTypeEncoder} for the specified field if present;
     * otherwise, finds the appropriate encoder using {@code findEncoder}, caches it, and returns it.
     *
     * <p>This method may throw a {@link PacketSerializationException} if no suitable encoder
     * is found for the given field. Internally, this method uses a cache to avoid redundant encoder lookups for the same field.</p>
     *
     * @param field  the field for which to retrieve the encoder
     * @param object the value of the field, used as context to find the encoder if needed
     * @return the cached or newly found {@link PacketTypeEncoder} for the field
     * @throws PacketSerializationException if no encoder can be found or an error occurs during lookup
     */
    public static PacketTypeEncoder<?> getEncoder(Field field, Object object) throws PacketSerializationException {
        try {
            return CACHED_ENCODERS.computeIfAbsent(field, f -> {
                try {
                    return PacketTypeSerializer.findEncoder(f, object);
                } catch (PacketSerializationException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException runtimeException) {
            Throwable cause = runtimeException.getCause();
            if (cause instanceof PacketSerializationException) {
                throw (PacketSerializationException) cause;
            }

            throw new PacketSerializationException(runtimeException.getMessage(), runtimeException);
        }
    }
}
