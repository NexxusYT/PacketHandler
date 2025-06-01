package com.github.razorplay.packet_handler.network.reflection.element.codec;

import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;

public final class TypeMatchCodecResolver<T> extends PrioritizedCodecResolver<T> {

    public TypeMatchCodecResolver(Class<T> type, PacketTypeCodec<T> codec) {
        super(
                context -> codec, // Static codec retrieve function
                context -> type.isAssignableFrom(context.getUnwrappedType())
        );
    }
}
