package com.github.razorplay.packet_handler.network.reflection;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.reflection.field.codec.PacketTypeDecoder;
import com.github.razorplay.packet_handler.network.reflection.field.codec.PacketTypeEncoder;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.PrioritizedFieldDecoder;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.PrioritizedFieldEncoder;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.impl.DelegatingDecoderModifier;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.impl.DelegatingEncoderModifier;
import com.github.razorplay.packet_handler.network.reflection.field.impl.BuiltInFieldDecoders;
import com.github.razorplay.packet_handler.network.reflection.field.impl.BuiltInFieldEncoders;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PacketTypeSerializer {

    private static final Map<Field, PacketTypeEncoder<?>> CACHED_ENCODERS = new HashMap<>();
    private static final Map<AnnotatedElement, PacketTypeDecoder<?>> CACHED_DECODERS = new HashMap<>();

    private static final PrioritizedFieldEncoder<?>[] SORTED_ENCODERS;
    private static final PrioritizedFieldEncoder<?>[] ENCODER_LIST = {
            BuiltInFieldEncoders.STRING_ENCODER,
            BuiltInFieldEncoders.INTEGER_ENCODER
    };

    private static final PrioritizedFieldDecoder<?>[] SORTED_DECODERS;
    private static final PrioritizedFieldDecoder<?>[] DECODER_LIST = {
            BuiltInFieldDecoders.STRING_DECODER,
            BuiltInFieldDecoders.INTEGER_DECODER
    };

    // List of predefined field encoder transformers (e.g., for nullable support).
    private static final DelegatingEncoderModifier<?, ?>[] ENCODER_MODIFIERS = {BuiltInFieldEncoders.NULLABLE_TRANSFORMER};
    private static final DelegatingDecoderModifier<?, ?>[] DECODER_MODIFIERS = {};

    static {
        SORTED_ENCODERS = Arrays.stream(ENCODER_LIST)
                // Create a sorted encoders using the priority property
                .sorted(Comparator.comparingInt(PrioritizedFieldEncoder::getPriority))
                .toArray(PrioritizedFieldEncoder<?>[]::new);

        SORTED_DECODERS = Arrays.stream(DECODER_LIST)
                .sorted(Comparator.comparingInt(PrioritizedFieldDecoder::getPriority))
                .toArray(PrioritizedFieldDecoder<?>[]::new);
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
                return encoder.get(field);
            }
        }

        throw new PacketSerializationException("No serializer found for field " + field.getName());
    }

    /**
     * Applies a given {@link DelegatingEncoderModifier} to transform the provided encoder.
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
    private static <T, U> PacketTypeEncoder<?> applyEncoderModifierIfMatches(
            DelegatingEncoderModifier<T, U> criteriaModifier,
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
     * Applies all registered {@link DelegatingEncoderModifier}s (transformers) that match the given field and value
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

        for (DelegatingEncoderModifier<?, ?> modifier : PacketTypeSerializer.ENCODER_MODIFIERS) {
            if (modifier.matches(field, unwrappedType, object)) {
                currentEncoder = applyEncoderModifierIfMatches(modifier, field, object, unwrappedType, currentEncoder);
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
     * otherwise, finds the appropriate encoder using {@code getEncoderWithTransformers}, caches it, and returns it.
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
                    return PacketTypeSerializer.getEncoderWithTransformers(f, object);
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

    public static PacketTypeDecoder<?> findDecoder(AnnotatedElement element, @Nullable Object object) throws PacketSerializationException {
        Class<?> unwrappedType;
        if (element instanceof Field) {
            Field field = (Field) element;
            unwrappedType = ReflectionUtil.unwrapBoxedType(field.getType());
        } else if (element instanceof Parameter) {
            Parameter field = (Parameter) element;
            unwrappedType = field.getType();
        } else {
            throw new PacketSerializationException("Unsupported element type: " + element.getClass().getName());
        }

        for (PrioritizedFieldDecoder<?> decoder : PacketTypeSerializer.SORTED_DECODERS) {
            if (decoder.matches(element, unwrappedType, object)) {
                return decoder.get(element);
            }
        }

        throw new PacketSerializationException("No deserializer found for field " + element);
    }

    @SuppressWarnings("unchecked")
    private static <T, U> PacketTypeDecoder<?> applyDecoderModifierIfMatches(
            DelegatingDecoderModifier<T, U> criteriaModifier,
            AnnotatedElement element,
            Object object,
            Class<?> unwrappedType,
            PacketTypeDecoder<?> typeDecoder
    ) {

        if (criteriaModifier.matches(element, unwrappedType, object)) {
            try {
                return criteriaModifier.transform((T) object, (PacketTypeDecoder<T>) typeDecoder);
            } catch (ClassCastException exception) {
                // Consider proper logging instead of printStackTrace
                exception.printStackTrace(System.out);
            }
        }

        return typeDecoder;
    }

    public static PacketTypeDecoder<?> applyTransformers(AnnotatedElement element, Object object, PacketTypeDecoder<?> typeDecoder) throws PacketSerializationException {
        PacketTypeDecoder<?> currentDecoder = typeDecoder;
        Class<?> unwrappedType = getaClass(element);

        for (DelegatingDecoderModifier<?, ?> modifier : PacketTypeSerializer.DECODER_MODIFIERS) {
            if (modifier.matches(element, unwrappedType, object)) {
                currentDecoder = applyDecoderModifierIfMatches(modifier, element, object, unwrappedType, currentDecoder);
            }
        }

        return currentDecoder;
    }

    private static Class<?> getaClass(AnnotatedElement element) throws PacketSerializationException {
        Class<?> unwrappedType;
        if (element instanceof Field) {
            Field field = (Field) element;
            unwrappedType = ReflectionUtil.unwrapBoxedType(field.getType());
        } else if (element instanceof Parameter) {
            Parameter field = (Parameter) element;
            unwrappedType = ReflectionUtil.unwrapBoxedType(field.getType());
        } else {
            throw new PacketSerializationException("Unsupported element type: " + element.getClass().getName());
        }
        return unwrappedType;
    }

    public static PacketTypeDecoder<?> getDecoderWithTransformers(AnnotatedElement element, Class<?> clazz) throws PacketSerializationException {
        return PacketTypeSerializer.applyTransformers(element, clazz, PacketTypeSerializer.findDecoder(element, clazz));
    }

    public static PacketTypeDecoder<?> getDecoder(AnnotatedElement element, Class<?> clazz) throws PacketSerializationException {
        try {
            return CACHED_DECODERS.computeIfAbsent(element, f -> {
                try {
                    return PacketTypeSerializer.getDecoderWithTransformers(f, clazz);
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
