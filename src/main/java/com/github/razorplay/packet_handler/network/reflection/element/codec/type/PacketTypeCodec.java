package com.github.razorplay.packet_handler.network.reflection.element.codec.type;

import lombok.Value;

@Value
public class PacketTypeCodec<T> {
    TypeEncoder<T> writer;
    TypeDecoder<T> reader;
}
