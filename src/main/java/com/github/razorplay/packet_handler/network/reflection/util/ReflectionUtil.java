package com.github.razorplay.packet_handler.network.reflection.util;

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
}
