package com.github.razorplay.packet_handler.network.reflection.element;

import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.reflection.element.codec.PrioritizedCodecResolver;
import com.github.razorplay.packet_handler.network.reflection.element.codec.TypeMatchCodecResolver;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;

/**
 * Provides a set of built-in {@link PacketTypeCodec} instances and corresponding
 * resolvers implementations for common types.
 *
 * <p>This class defines codecs and resolvers for primitive types, and
 * provides them in a statically sorted array based on their priority.</p>
 */
public final class BuiltInCodecs {

    private static final PacketTypeCodec<Byte> BYTE_CODEC = new PacketTypeCodec<>(PacketDataSerializer::writeByte, PacketDataSerializer::readByte);
    private static final PacketTypeCodec<Long> LONG_CODEC = new PacketTypeCodec<>(PacketDataSerializer::writeLong, PacketDataSerializer::readLong);
    private static final PacketTypeCodec<Short> SHORT_CODEC = new PacketTypeCodec<>(PacketDataSerializer::writeShort, PacketDataSerializer::readShort);
    private static final PacketTypeCodec<Float> FLOAT_CODEC = new PacketTypeCodec<>(PacketDataSerializer::writeFloat, PacketDataSerializer::readFloat);
    private static final PacketTypeCodec<Double> DOUBLE_CODEC = new PacketTypeCodec<>(PacketDataSerializer::writeDouble, PacketDataSerializer::readDouble);
    private static final PacketTypeCodec<String> STRING_CODEC = new PacketTypeCodec<>(PacketDataSerializer::writeString, PacketDataSerializer::readString);
    private static final PacketTypeCodec<Boolean> BOOLEAN_CODEC = new PacketTypeCodec<>(PacketDataSerializer::writeBoolean, PacketDataSerializer::readBoolean);
    private static final PacketTypeCodec<Integer> INTEGER_CODEC = new PacketTypeCodec<>(PacketDataSerializer::writeInt, PacketDataSerializer::readInt);
    private static final PacketTypeCodec<Character> CHAR_CODEC = new PacketTypeCodec<>(PacketDataSerializer::writeChar, PacketDataSerializer::readChar);

    public static PrioritizedCodecResolver<Byte> BYTE_RESOLVER = new TypeMatchCodecResolver<>(byte.class, BuiltInCodecs.BYTE_CODEC);
    public static PrioritizedCodecResolver<Long> LONG_RESOLVER = new TypeMatchCodecResolver<>(long.class, BuiltInCodecs.LONG_CODEC);
    public static PrioritizedCodecResolver<Short> SHORT_RESOLVER = new TypeMatchCodecResolver<>(short.class, BuiltInCodecs.SHORT_CODEC);
    public static PrioritizedCodecResolver<Float> FLOAT_RESOLVER = new TypeMatchCodecResolver<>(float.class, BuiltInCodecs.FLOAT_CODEC);
    public static PrioritizedCodecResolver<Double> DOUBLE_RESOLVER = new TypeMatchCodecResolver<>(double.class, BuiltInCodecs.DOUBLE_CODEC);
    public static PrioritizedCodecResolver<String> STRING_RESOLVER = new TypeMatchCodecResolver<>(String.class, BuiltInCodecs.STRING_CODEC);
    public static PrioritizedCodecResolver<Boolean> BOOLEAN_RESOLVER = new TypeMatchCodecResolver<>(boolean.class, BuiltInCodecs.BOOLEAN_CODEC);
    public static PrioritizedCodecResolver<Integer> INTEGER_RESOLVER = new TypeMatchCodecResolver<>(int.class, BuiltInCodecs.INTEGER_CODEC);
    public static PrioritizedCodecResolver<Character> CHAR_RESOLVER = new TypeMatchCodecResolver<>(char.class, BuiltInCodecs.CHAR_CODEC);
    
    private static final PrioritizedCodecResolver<?>[] RESOLVERS = new PrioritizedCodecResolver[]{
            BuiltInCodecs.BYTE_RESOLVER,
            BuiltInCodecs.LONG_RESOLVER,
            BuiltInCodecs.SHORT_RESOLVER,
            BuiltInCodecs.FLOAT_RESOLVER,
            BuiltInCodecs.DOUBLE_RESOLVER,
            BuiltInCodecs.STRING_RESOLVER,
            BuiltInCodecs.BOOLEAN_RESOLVER,
            BuiltInCodecs.INTEGER_RESOLVER,
            BuiltInCodecs.CHAR_RESOLVER
    };

    /**
     * Array of resolvers sorted by priority in descending order.
     */
    public static final PrioritizedCodecResolver<?>[] SORTED_RESOLVERS = PrioritizedCodecResolver.sort(RESOLVERS);
}