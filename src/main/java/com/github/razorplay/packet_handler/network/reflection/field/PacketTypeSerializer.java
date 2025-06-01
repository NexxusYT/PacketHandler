package com.github.razorplay.packet_handler.network.reflection.field;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.impl.BuiltInFieldCriteria;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.impl.FieldCriteriaModifierImpl;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.impl.FieldCriteriaSerializerImpl;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;

public class PacketTypeSerializer {

    private static final FieldCriteriaSerializerImpl<?>[] SORTED_CRITERIA;
    private static final FieldCriteriaSerializerImpl<?>[] CRITERIA_LIST = {
            BuiltInFieldCriteria.STRING_CRITERIA,
            BuiltInFieldCriteria.INTEGER_CRITERIA
    };

    private static final FieldCriteriaModifierImpl<?, ?>[] CRITERIA_MODIFIER_LIST = {BuiltInFieldCriteria.NULLABLE_MODIFIER};

    static {
        SORTED_CRITERIA = Arrays.stream(CRITERIA_LIST)
                .sorted(BuiltInFieldCriteria.CRITERIA_COMPARATOR)
                .toArray(FieldCriteriaSerializerImpl<?>[]::new);
    }

    public static TypeEncoder<?> retrieve(Field field, @Nullable Object object) throws PacketSerializationException {
        final Class<?> unwrappedType = PacketTypeSerializer.unwrapBoxedType(field.getType());

        for (FieldCriteriaSerializerImpl<?> criteria : PacketTypeSerializer.SORTED_CRITERIA) {
            if (criteria.matches(field, unwrappedType, object)) {
                return criteria.retrieveSerializer(field);
            }
        }

        throw new PacketSerializationException("No serializer found for field " + field.getName());
    }

    @SuppressWarnings("unchecked")
    private static <T, U> TypeEncoder<?> modify(FieldCriteriaModifierImpl<T, U> criteriaModifier, Field field, Class<?> unwrappedType, @Nullable Object object, TypeEncoder<?> typeEncoder) {
        if (criteriaModifier.matches(field, unwrappedType, object)) {
            try {
                return criteriaModifier.modify((T) object, (TypeEncoder<T>) typeEncoder);
            } catch (ClassCastException exception) {
                exception.printStackTrace(System.out);
            }
        }

        return typeEncoder;
    }

    public static TypeEncoder<?> modify(Field field, @Nullable Object object, TypeEncoder<?> typeEncoder) {
        TypeEncoder<?> currentEncoder = typeEncoder;
        final Class<?> unwrappedType = PacketTypeSerializer.unwrapBoxedType(field.getType());

        for (FieldCriteriaModifierImpl<?, ?> modifier : PacketTypeSerializer.CRITERIA_MODIFIER_LIST) {
            if (modifier.matches(field, unwrappedType, object)) {
                currentEncoder = modify(modifier, field, unwrappedType, object, currentEncoder);
            }
        }

        return currentEncoder;
    }

    public static TypeEncoder<?> retrieveWithModifiers(Field field, @Nullable Object object) throws PacketSerializationException {
        return PacketTypeSerializer.modify(field, object, PacketTypeSerializer.retrieve(field, object));
    }

    /**
     * Converts boxed types (e.g., {@code Integer}) to their corresponding primitive types (e.g., {@code int}).<br>
     * If the type is not a known wrapper, the original class is returned.
     *
     * @param clazz The type to unwrap.
     * @return The primitive equivalent or the same class if unrecognized.
     */
    public static Class<?> unwrapBoxedType(Class<?> clazz) {
        if (clazz == Integer.class) return int.class;
        if (clazz == Boolean.class) return boolean.class;
        if (clazz == Double.class) return double.class;
        if (clazz == Float.class) return float.class;
        if (clazz == Short.class) return short.class;
        if (clazz == Long.class) return long.class;
        if (clazz == Byte.class) return byte.class;

        return clazz;
    }
}
