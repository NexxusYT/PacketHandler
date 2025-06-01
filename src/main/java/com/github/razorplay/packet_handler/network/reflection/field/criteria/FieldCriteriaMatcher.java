package com.github.razorplay.packet_handler.network.reflection.field.criteria;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

@FunctionalInterface
public interface FieldCriteriaMatcher {

    boolean matches(Field field, Class<?> unwrappedType, @Nullable Object value);
}
