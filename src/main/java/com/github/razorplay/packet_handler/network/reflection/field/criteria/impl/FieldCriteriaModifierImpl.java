package com.github.razorplay.packet_handler.network.reflection.field.criteria.impl;

import com.github.razorplay.packet_handler.network.reflection.field.TypeEncoder;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.FieldCriteriaMatcher;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.FieldCriteriaModifier;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class FieldCriteriaModifierImpl<T, U> implements FieldCriteriaModifier<T, U>, FieldCriteriaMatcher {

    private final FieldCriteriaMatcher fieldMatch;
    private final FieldCriteriaModifier<T, U> fieldModifier;

    public FieldCriteriaModifierImpl(FieldCriteriaMatcher fieldMatch, FieldCriteriaModifier<T, U> fieldModifier) {
        this.fieldMatch = fieldMatch;
        this.fieldModifier = fieldModifier;
    }

    @Override
    public boolean matches(Field field, Class<?> unwrappedType, @Nullable Object value) {
        return fieldMatch.matches(field, unwrappedType, value);
    }

    @Override
    public TypeEncoder<U> modify(T object, TypeEncoder<T> typeEncoder) {
        return fieldModifier.modify(object, typeEncoder);
    }
}
