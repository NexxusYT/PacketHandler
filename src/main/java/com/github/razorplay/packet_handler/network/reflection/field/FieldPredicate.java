package com.github.razorplay.packet_handler.network.reflection.field;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;

/**
 * Functional interface for matching fields based on custom criteria,
 * such as annotations, types, or runtime values.
 */
@FunctionalInterface
public interface FieldPredicate {

    /**
     * Determines whether the given field satisfies a certain condition.
     *
     * @param field         the field being evaluated.
     * @param unwrappedType the resolved primitive or boxed type of the field.
     * @param value         the runtime value of the field (maybe {@code null}).
     * @return {@code true} if the field matches the criteria, {@code false} otherwise.
     */
    boolean matches(AnnotatedElement field, Class<?> unwrappedType, @Nullable Object value);
}
