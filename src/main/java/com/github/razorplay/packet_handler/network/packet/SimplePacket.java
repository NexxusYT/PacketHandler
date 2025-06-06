package com.github.razorplay.packet_handler.network.packet;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.IPacket;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.reflection.ClassSerializer;
import com.github.razorplay.packet_handler.network.reflection.element.AnnotatedElementContext;

public interface SimplePacket extends IPacket {

    @Override
    default void write(final PacketDataSerializer serializer) throws PacketSerializationException {
        ClassSerializer.encode(serializer, AnnotatedElementContext.ofClass(this));
    }

    @Override
    default void read(final PacketDataSerializer serializer) throws PacketSerializationException {
        throw new PacketSerializationException("Simple packets could not be deserialized with the read method. Instead use ClassSerializer#decode");
    }
}
