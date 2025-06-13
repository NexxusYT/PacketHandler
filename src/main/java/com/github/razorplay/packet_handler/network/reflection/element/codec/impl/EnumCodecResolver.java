package com.github.razorplay.packet_handler.network.reflection.element.codec.impl;

import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.reflection.element.codec.PrioritizedCodecResolver;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;

/**
 * A final class that resolves codecs for enum types, extending {@link PrioritizedCodecResolver}.
 * Provides a singleton instance for encoding and decoding enum types in packet data serialization.
 */
public final class EnumCodecResolver extends PrioritizedCodecResolver {

    /**
     * Singleton instance of {@link EnumCodecResolver} for global access.
     */
    public static final EnumCodecResolver INSTANCE = new EnumCodecResolver();

    /**
     * Private constructor to enforce singleton pattern and initialize the resolver with
     * a codec factory and type predicate for enum types.
     */
    private EnumCodecResolver() {
        super(
                context -> EnumCodecResolver.createEnumCodec(context.getUnwrappedType()),
                context -> context.getUnwrappedType().isEnum()
        );
    }

    /**
     * Creates an {@link EnumCodec} for the provided type, ensuring it is an enum.
     *
     * @param unwrappedType The class type to create a codec for.
     * @param <T>           The enum type extending {@link Enum}.
     * @return An {@link EnumCodec} for the specified enum type.
     * @throws IllegalArgumentException If the provided type is not an enum.
     */
    private static <T extends Enum<T>> EnumCodec<T> createEnumCodec(Class<?> unwrappedType) {
        if (!unwrappedType.isEnum()) {
            throw new IllegalArgumentException("The provided type is not an enum.");
        }
        @SuppressWarnings("unchecked")
        Class<T> enumClass = (Class<T>) unwrappedType;
        return new EnumCodec<>(enumClass);
    }

    /**
     * A static inner class for encoding and decoding enum types in packet data serialization.
     *
     * @param <T> The enum type extending {@link Enum}.
     */
    static final class EnumCodec<T extends Enum<T>> extends PacketTypeCodec<T> {

        /**
         * Constructs an {@link EnumCodec} for the specified enum class, defining
         * how to read and write enum values using a {@link PacketDataSerializer}.
         *
         * @param enumClass The class of the enum to encode and decode.
         */
        public EnumCodec(Class<T> enumClass) {
            super(PacketDataSerializer::writeEnum, reader -> reader.readEnum(enumClass));
        }
    }
}
