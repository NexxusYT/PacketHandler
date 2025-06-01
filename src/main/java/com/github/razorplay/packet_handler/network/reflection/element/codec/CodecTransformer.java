package com.github.razorplay.packet_handler.network.reflection.element.codec;

import com.github.razorplay.packet_handler.network.reflection.element.AnnotatedElementContext;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;

@FunctionalInterface
public interface CodecTransformer {

    <T, U> PacketTypeCodec<U> transform(AnnotatedElementContext context, PacketTypeCodec<T> codec);
}
