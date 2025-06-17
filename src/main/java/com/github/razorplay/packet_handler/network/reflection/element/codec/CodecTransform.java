package com.github.razorplay.packet_handler.network.reflection.element.codec;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.network_util.ThrowingBiConsumer;
import com.github.razorplay.packet_handler.network.network_util.ThrowingFunction;
import com.github.razorplay.packet_handler.network.reflection.element.AnnotatedElementContext;
import com.github.razorplay.packet_handler.network.reflection.element.ElementPredicate;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.TypeDecoder;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.TypeEncoder;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.TypeTransform;

/**
 * An abstract class that implements the {@link TypeTransform} interface to transform packet codecs.
 * It applies encoding and decoding transformations based on a predicate matching condition.
 */
public abstract class CodecTransform implements TypeTransform {

    /* The predicate used to determine if the transformation should be applied to an element context. */
    private final ElementPredicate elementPredicate;

    /**
     * Constructs a CodecTransform with the specified element predicate.
     *
     * @param elementPredicate the predicate to evaluate if the transformation applies
     */
    public CodecTransform(ElementPredicate elementPredicate) {
        this.elementPredicate = elementPredicate;
    }

    /**
     * Applies a codec transformation if the context matches the predicate.
     *
     * @param <T>     the type of the codec
     * @param context the annotated element context
     * @param codec   the original codec to transform
     * @return a new {@link PacketTypeCodec} with transformed encoder and decoder if the context matches,
     * otherwise returns the original codec
     */
    @Override
    public <T> PacketTypeCodec<T> applyCodec(AnnotatedElementContext context, PacketTypeCodec<T> codec) {
        if (this.matches(context)) {
            return new PacketTypeCodec<T>(
                    this.applyEncoder(codec.getWriter()),
                    this.applyDecoder(codec.getReader())
            );
        }
        return codec;
    }

    /**
     * Checks if the transformation should be applied to the given context.
     *
     * @param context the annotated element context to evaluate
     * @return true if the predicate matches the context, false otherwise
     */
    @Override
    public boolean matches(AnnotatedElementContext context) {
        return this.elementPredicate.matches(context);
    }

    /**
     * Applies an encoder transformation.
     *
     * @param <T>     the type of the value to encode
     * @param encoder the original encoder
     * @return a new {@link TypeEncoder} that applies middleware encoding
     */
    @Override
    public <T> TypeEncoder<T> applyEncoder(TypeEncoder<T> encoder) {
        return (writer, value) -> this.middlewareEncode(writer, value, encoder::encode);
    }

    /**
     * Applies a decoder transformation.
     *
     * @param <T>     the type of the value to decode
     * @param decoder the original decoder
     * @return a new {@link TypeDecoder} that applies middleware decoding
     */
    @Override
    public <T> TypeDecoder<T> applyDecoder(TypeDecoder<T> decoder) {
        return reader -> this.middlewareAfterDecode(reader, decoder::decode);
    }

    /**
     * Middleware method for encoding, allowing subclasses to customize the encoding process.
     *
     * @param <T>     the type of the value to encode
     * @param writer  the serializer to write to
     * @param value   the value to encode
     * @param encoder the original encoder function
     * @throws PacketSerializationException if encoding fails
     */
    protected <T> void middlewareEncode(PacketDataSerializer writer, T value, ThrowingBiConsumer<PacketDataSerializer, T> encoder) throws PacketSerializationException {
        encoder.accept(writer, value);
    }

    /**
     * Middleware method for decoding, allowing subclasses to customize the decoding process.
     *
     * @param <T>     the type of the value to decode
     * @param reader  the serializer to read from
     * @param decoder the original decoder function
     * @return the decoded value
     * @throws PacketSerializationException if decoding fails
     */
    protected <T> T middlewareAfterDecode(PacketDataSerializer reader, ThrowingFunction<PacketDataSerializer, T> decoder) throws PacketSerializationException {
        return decoder.apply(reader);
    }
}
