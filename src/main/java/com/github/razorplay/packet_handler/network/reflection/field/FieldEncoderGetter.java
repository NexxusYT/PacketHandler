package com.github.razorplay.packet_handler.network.reflection.field;

import java.lang.reflect.Field;

@FunctionalInterface
public interface FieldEncoderGetter<T> {

    TypeEncoder<T> retrieveSerializer(Field field);
}
