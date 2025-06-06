package com.github.razorplay.packet_handler.network.reflection.element;

import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.reflection.element.codec.PrioritizedCodecResolver;
import com.github.razorplay.packet_handler.network.reflection.element.codec.TypeMatchCodecResolver;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;

/**
 * Provides a set of built-in {@link PacketTypeCodec} instances and corresponding
 * resolvers implementations for common types.
 *
 * <p>This class defines codecs and resolvers for {@code String} and {@code int}, and
 * provides them in a statically sorted array based on their priority.</p>
 */
public final class BuiltInCodecs {

    private static final PacketTypeCodec<String> STRING_CODEC = new PacketTypeCodec<>(PacketDataSerializer::writeString, PacketDataSerializer::readString);
    private static final PacketTypeCodec<Integer> INTEGER_CODEC = new PacketTypeCodec<>(PacketDataSerializer::writeInt, PacketDataSerializer::readInt);

    public static PrioritizedCodecResolver<String> STRING_RESOLVER = new TypeMatchCodecResolver<>(String.class, BuiltInCodecs.STRING_CODEC);
    public static PrioritizedCodecResolver<Integer> INTEGER_RESOLVER = new TypeMatchCodecResolver<>(int.class, BuiltInCodecs.INTEGER_CODEC);

    private static final PrioritizedCodecResolver<?>[] RESOLVERS = new PrioritizedCodecResolver[]{
            BuiltInCodecs.STRING_RESOLVER,
            BuiltInCodecs.INTEGER_RESOLVER
    };
    
    /**
     * Array of resolvers sorted by priority in descending order.
     */
    public static final PrioritizedCodecResolver<?>[] SORTED_RESOLVERS = PrioritizedCodecResolver.sort(RESOLVERS);
}