package com.github.razorplay.packet_handler.network.reflection.element.codec.type;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;

@FunctionalInterface
public interface TypeDecoder<T> {

    T decode(PacketDataSerializer reader) throws PacketSerializationException;
}
