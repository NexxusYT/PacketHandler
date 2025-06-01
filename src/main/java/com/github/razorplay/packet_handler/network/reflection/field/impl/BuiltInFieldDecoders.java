package com.github.razorplay.packet_handler.network.reflection.field.impl;

import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.PrioritizedFieldDecoder;
import com.github.razorplay.packet_handler.network.reflection.field.criteria.impl.TypeMatchDecoder;

public final class BuiltInFieldDecoders {

    public static final PrioritizedFieldDecoder<String> STRING_DECODER = new TypeMatchDecoder<>(String.class, PacketDataSerializer::readString);
    public static final PrioritizedFieldDecoder<Integer> INTEGER_DECODER = new TypeMatchDecoder<>(int.class, PacketDataSerializer::readInt);
}
