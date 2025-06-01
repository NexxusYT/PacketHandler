package com.github.razorplay.packet_handler.network.network_util;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.reflection.field.PacketTypeSerializer;
import com.github.razorplay.packet_handler.network.reflection.field.TypeEncoder;

import java.lang.reflect.Field;

public final class PacketSerializer {

    @SuppressWarnings("unchecked")
    private static <T> void encodeField(PacketDataSerializer writer, Object object, Field declaredField) throws PacketSerializationException {
        try {
            declaredField.setAccessible(true);
            T value = (T) declaredField.get(object);

            TypeEncoder<T> typeEncoder = (TypeEncoder<T>) PacketTypeSerializer.retrieveWithModifiers(declaredField, value);
            typeEncoder.encode(writer, value);
        } catch (IllegalAccessException e) {
            throw new PacketSerializationException("Failed to encode packet", e);
        }
    }

    /**
     * Serializes all declared fields of the given packet object using the provided data writer.
     * <p>
     * Supports primitive types and fields implementing {@link CustomSerializable}.
     *
     * @param object The object to serialize.
     * @param writer The writer used to output the serialized data.
     * @param <T>    The type of the packet object.
     * @throws PacketSerializationException if any field access or encoding fails.
     */
    public static <T> void serialize(T object, PacketDataSerializer writer) throws PacketSerializationException {
        for (Field declaredField : object.getClass().getDeclaredFields()) {
            if (declaredField.isSynthetic()) continue;
            encodeField(writer, object, declaredField);
        }
    }
}
