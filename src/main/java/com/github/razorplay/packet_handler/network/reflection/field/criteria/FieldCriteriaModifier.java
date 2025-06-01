package com.github.razorplay.packet_handler.network.reflection.field.criteria;

import com.github.razorplay.packet_handler.network.reflection.field.TypeEncoder;

@FunctionalInterface
public interface FieldCriteriaModifier<T, V> {

    TypeEncoder<V> modify(T object, TypeEncoder<T> typeEncoder);
}
