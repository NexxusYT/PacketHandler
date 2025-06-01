package com.github.razorplay.packet_handler.network.reflection.field.codec.resolver;

import com.github.razorplay.packet_handler.network.reflection.field.GenericTypeAction;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

/**
 * Resolves and returns an instance of {@code U} (which extends {@link GenericTypeAction <T>})
 * associated with the given {@link Field}.
 *
 * @param <T> the target type that the {@code GenericTypeAction} operates on
 * @param <U> the specific subtype of {@link GenericTypeAction} to be returned
 */
public interface FieldActionResolver<T, U extends GenericTypeAction<T>> {

    /**
     * Returns the {@code GenericTypeAction} instance associated with the specified field.
     *
     * @param field the field for which to resolve the {@code GenericTypeAction}
     * @return an instance of {@code U} corresponding to the given field
     */
    U get(AnnotatedElement field);
}
