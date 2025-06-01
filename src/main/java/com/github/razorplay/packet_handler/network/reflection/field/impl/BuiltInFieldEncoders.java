package com.github.razorplay.packet_handler.network.reflection.field.impl;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.PrioritizedFieldEncoder;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.impl.DelegatingEncoderModifier;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.impl.TypeMatchEncoder;

import javax.annotation.Nullable;

public final class BuiltInFieldEncoders {

    /**
     * An encoder transformer that applies nullable encoding logic
     * if the field value is null or annotated with {@link Nullable}.
     */
    public static final DelegatingEncoderModifier<Object, Object> NULLABLE_TRANSFORMER = new DelegatingEncoderModifier<>(
            (field, unwrappedType, object) -> object == null || field.isAnnotationPresent(Nullable.class),
            (object, typeEncoder) -> // Encoder transform output function
                    (writer, value) -> writer.writeNullable(value, (dataSerializer, val) -> {
                        try {
                            typeEncoder.encode(dataSerializer, val);
                        } catch (PacketSerializationException exception) {
                            // Consider proper logging or rethrowing here
                            exception.printStackTrace(System.out);
                        }
                    })
    );

    public static final PrioritizedFieldEncoder<String> STRING_ENCODER = new TypeMatchEncoder<>(String.class, PacketDataSerializer::writeString);
    public static final PrioritizedFieldEncoder<Integer> INTEGER_ENCODER = new TypeMatchEncoder<>(int.class, PacketDataSerializer::writeInt);
}
