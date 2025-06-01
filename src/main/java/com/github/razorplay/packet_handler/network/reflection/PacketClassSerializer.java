package com.github.razorplay.packet_handler.network.reflection;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.CustomSerializable;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.reflection.field.codec.PacketTypeEncoder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public final class PacketClassSerializer {

    /**
     * Encodes the value of a given field from an object using the appropriate {@link PacketTypeEncoder}.
     *
     * <p>This method uses reflection to access the field value, then retrieves the corresponding encoder
     * and encodes the value into the provided {@link PacketDataSerializer}.</p>
     *
     * @param writer        the {@link PacketDataSerializer} to write encoded data into
     * @param object        the object containing the field value to encode
     * @param declaredField the field whose value will be encoded
     * @param <T>           the type of the field value
     * @throws PacketSerializationException if an error occurs accessing the field or encoding the value
     */
    @SuppressWarnings("unchecked")
    private static <T> void encodeField(PacketDataSerializer writer, Object object, Field declaredField) throws PacketSerializationException {
        try {
            declaredField.setAccessible(true);
            T value = (T) declaredField.get(object);

            PacketTypeEncoder<T> typeEncoder = (PacketTypeEncoder<T>) PacketTypeSerializer.getEncoder(declaredField, value);
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

    public static <T> T deserialize(PacketDataSerializer reader, Class<T> packetClass) throws PacketSerializationException, NoSuchMethodException {
        Constructor<?>[] constructors = packetClass.getDeclaredConstructors();
        Constructor<?> constructor = constructors.length == 0
                ? packetClass.getDeclaredConstructor()
                : constructors[0];

        constructor.setAccessible(true);
        Object[] parameters = new Object[constructor.getParameterCount()];

        int count = 0;
        for (Parameter parameter : constructor.getParameters()) {
            if (parameter.isSynthetic()) continue;
            parameters[count++] = PacketTypeSerializer.getDecoder(parameter, parameter.getType()).decode(reader);
        }

        try {
            return (T) constructor.newInstance(parameters);
        } catch (ReflectiveOperationException e) {
            throw new PacketSerializationException("Failed to instantiate custom object", e);
        }
    }
}
