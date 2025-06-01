package com.github.razorplay.packet_handler.network.reflection.field.codec.resolver;

import com.github.razorplay.packet_handler.network.reflection.field.codec.PacketTypeDecoder;

/**
 * @param <T> the type to be decoded by the {@link PacketTypeDecoder}
 */
@FunctionalInterface
public interface FieldDecoderResolver<T> extends FieldActionResolver<T, PacketTypeDecoder<T>> {
}
