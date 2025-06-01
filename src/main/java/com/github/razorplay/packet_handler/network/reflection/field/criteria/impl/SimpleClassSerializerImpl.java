package com.github.razorplay.packet_handler.network.reflection.field.criteria.impl;

import com.github.razorplay.packet_handler.network.reflection.field.TypeEncoder;

public class SimpleClassSerializerImpl<T> extends FieldCriteriaSerializerImpl<T> {

    public SimpleClassSerializerImpl(Class<T> typeClass, TypeEncoder<T> typeEncoder) {
        super((field, unwrappedType, object) -> unwrappedType.isAssignableFrom(typeClass), field -> typeEncoder);
    }
}
