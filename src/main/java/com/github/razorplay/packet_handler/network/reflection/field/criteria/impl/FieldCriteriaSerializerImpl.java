package com.github.razorplay.packet_handler.network.reflection.field.criteria.impl;

import com.github.razorplay.packet_handler.network.reflection.field.FieldEncoderGetter;
import com.github.razorplay.packet_handler.network.reflection.field.TypeEncoder;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.FieldCriteriaMatcher;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.FieldCriteriaSerializer;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class FieldCriteriaSerializerImpl<T> implements FieldCriteriaSerializer<T> {

    private final int priority;
    private final FieldCriteriaMatcher fieldMatch;
    private final FieldEncoderGetter<T> fieldSerializerGetter;

    public FieldCriteriaSerializerImpl(FieldCriteriaMatcher fieldMatch, FieldEncoderGetter<T> fieldSerializerGetter) {
        this(0, fieldMatch, fieldSerializerGetter);
    }

    public FieldCriteriaSerializerImpl(int priority, FieldCriteriaMatcher fieldMatch, FieldEncoderGetter<T> fieldSerializerGetter) {
        this.priority = priority;
        this.fieldMatch = fieldMatch;
        this.fieldSerializerGetter = fieldSerializerGetter;
    }

    @Override
    public boolean matches(Field field, Class<?> unwrappedType, @Nullable Object value) {
        return fieldMatch.matches(field, unwrappedType, value);
    }

    @Override
    public TypeEncoder<T> retrieveSerializer(Field field) {
        return fieldSerializerGetter.retrieveSerializer(field);
    }

    @Override
    public int getPriority() {
        return this.priority;
    }
}
