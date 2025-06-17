package com.github.razorplay.packet_handler.network.reflection.util;

import javax.annotation.Nullable;
import java.lang.reflect.*;

public final class ReflectionUtil {

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

    /**
     * Retrieves the parameterized type at the specified index from a given type.
     * Returns null if the type is not parameterized or the index is out of bounds.
     *
     * @param type  the type to inspect for parameterization
     * @param index the index of the parameterized type argument to retrieve
     * @return the {@link Type} at the specified index, or null if not found
     */
    @Nullable
    public static Type getParameterizedType(Type type, int index) {
        // Check if the type is not parameterized
        if (!(type instanceof ParameterizedType)) {
            return null;
        }

        // Cast to ParameterizedType and get actual type arguments
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        // Validate index bounds
        if (actualTypeArguments.length == 0 || index >= actualTypeArguments.length) {
            return null;
        }

        // Return the type argument at the specified index
        return actualTypeArguments[index];
    }

    /**
     * Retrieves the generic type from an annotated element.
     * Supports parameters, classes, fields, and methods.
     *
     * @param annotatedElement the annotated element to extract the generic type from
     * @return the generic {@link Type} of the element
     * @throws IllegalArgumentException if the annotated element type is unsupported
     */
    private static Type getGenericType(AnnotatedElement annotatedElement) {
        if (annotatedElement instanceof Parameter) return ((Parameter) annotatedElement).getParameterizedType();
        if (annotatedElement instanceof Class) return ((Class<?>) annotatedElement).getGenericSuperclass();
        if (annotatedElement instanceof Field) return ((Field) annotatedElement).getGenericType();
        if (annotatedElement instanceof Method) return ((Method) annotatedElement).getGenericReturnType();

        throw new IllegalArgumentException("Unsupported annotated element: " + annotatedElement);
    }

    /**
     * Retrieves the first parameterized type from an annotated element.
     * Combines generic type extraction with parameterized type retrieval.
     *
     * @param annotatedElement the annotated element to extract the parameterized type from
     * @return the first parameterized {@link Type}, or null if not found
     */
    public static Type getParameterizedType(AnnotatedElement annotatedElement) {
        // Extract the generic type and get the first parameterized type
        return getParameterizedType(getGenericType(annotatedElement), 0);
    }
}
