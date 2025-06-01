package com.github.razorplay.packet_handler.network.reflection.field.codec.resolver;

import com.github.razorplay.packet_handler.network.reflection.field.codec.PacketTypeEncoder;

/**
 * @param <T> the type to be encoded by the {@link PacketTypeEncoder}
 */
@FunctionalInterface
public interface FieldEncoderResolver<T> extends FieldActionResolver<T, PacketTypeEncoder<T>> {
}
