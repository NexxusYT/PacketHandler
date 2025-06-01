package com.github.razorplay.packet_handler.network.reflection.field.criteria.impl;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.FieldCriteriaSerializer;

import javax.annotation.Nullable;
import java.util.Comparator;

public class BuiltInFieldCriteria {

    public static final Comparator<FieldCriteriaSerializer<?>> CRITERIA_COMPARATOR = (o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority());

    public static final FieldCriteriaModifierImpl<Object, Object> NULLABLE_MODIFIER = new FieldCriteriaModifierImpl<>(
            (field, unwrappedType, object) -> object == null || field.isAnnotationPresent(Nullable.class),
            (object, typeEncoder) -> (writer, value) -> writer.writeNullable(value, (dataSerializer, val) -> {
                try {
                    typeEncoder.encode(dataSerializer, val);
                } catch (PacketSerializationException exception) {
                    exception.printStackTrace(System.out);
                }
            })
    );

    public static final FieldCriteriaSerializerImpl<String> STRING_CRITERIA = new SimpleClassSerializerImpl<>(String.class, PacketDataSerializer::writeString);
    public static final FieldCriteriaSerializerImpl<Integer> INTEGER_CRITERIA = new SimpleClassSerializerImpl<>(int.class, PacketDataSerializer::writeInt);
}
