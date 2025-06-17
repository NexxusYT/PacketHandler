package com.github.razorplay.packet_handler.network.reflection.element.codec.impl;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.packet.annotation.ParallelStreamable;
import com.github.razorplay.packet_handler.network.packet.annotation.Streamable;
import com.github.razorplay.packet_handler.network.reflection.ClassSerializer;
import com.github.razorplay.packet_handler.network.reflection.element.AnnotatedElementContext;
import com.github.razorplay.packet_handler.network.reflection.element.codec.PrioritizedCodecResolver;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;
import com.github.razorplay.packet_handler.network.reflection.util.ReflectionUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A resolver for handling streamable data types in packet serialization, extending the prioritized codec resolver.
 * This class supports serialization and deserialization of streams, arrays, and iterables, with configurable parallel processing.
 */
public final class StreamableCodecResolver extends PrioritizedCodecResolver {

    public static final StreamableCodecResolver INSTANCE = new StreamableCodecResolver();

    /**
     * Constructs a StreamableCodecResolver with the specified parallel processing option.
     */
    private StreamableCodecResolver() {
        super(
                StreamableCodecResolver::createStreamCodec,
                StreamableCodecResolver::isStreamable
        );
    }

    /**
     * Creates a StreamCodec for the given annotated element context and parallel processing setting.
     *
     * @param elementContext the context containing the annotated element and its type
     * @return a StreamCodec instance for the specified type
     * @throws IllegalArgumentException if the type is not streamable
     */
    private static StreamCodec<?> createStreamCodec(AnnotatedElementContext elementContext) {
        Class<?> unwrappedType = elementContext.getUnwrappedType();
        AnnotatedElement element = elementContext.getAnnotatedElement();
        boolean annotatedWithParallel = element.isAnnotationPresent(ParallelStreamable.class);

        Class<?> type = StreamableCodecResolver.retrieveTypeFrom(element, unwrappedType, annotatedWithParallel);
        return new StreamCodec<>(unwrappedType, type, annotatedWithParallel);
    }

    /**
     * Retrieves the component type for streaming operations from an annotated element.
     * This method inspects the element's generic type, array component type, or streamable annotations
     * to determine the appropriate type for serialization.
     *
     * @param element        the annotated element (e.g., Field or Parameter) to inspect
     * @param unwrappedClass the unwrapped class type of the element
     * @param parallel       true if parallel streaming is enabled, false otherwise
     * @return the Class representing the component type for streaming
     * @throws IllegalArgumentException if no valid streamable type can be determined
     * @throws NullPointerException     if the element or unwrappedClass is null
     */
    private static Class<?> retrieveTypeFrom(AnnotatedElement element, Class<?> unwrappedClass, boolean parallel) {
        try {
            Type parameterizedType = ReflectionUtil.getParameterizedType(element);
            if (parameterizedType instanceof Class) {
                return (Class<?>) parameterizedType;
            }
        } catch (IllegalArgumentException ignored) {
        }

        if (unwrappedClass.isArray()) {
            return unwrappedClass.getComponentType();
        }
        if (parallel) return element.getAnnotation(ParallelStreamable.class).value();
        if (element.isAnnotationPresent(Streamable.class)) {
            return element.getAnnotation(Streamable.class).value();
        }

        throw new IllegalArgumentException("Cannot create stream codec for " + unwrappedClass.getName());
    }

    /**
     * Recovers a stream into the specified target class type.
     *
     * @param <T>         the type of the target class
     * @param stream      the input stream to recover
     * @param objectClass the target class to convert the stream to
     * @return the converted object of type T
     * @throws IllegalArgumentException if the stream cannot be converted to the target class
     */
    @SuppressWarnings("unchecked")
    private static <T> T recoverStream(Stream<?> stream, Class<T> objectClass) {
        // Direct stream return if target is Stream
        if (objectClass.isAssignableFrom(Stream.class)) return (T) stream;

        // Convert to List or other Collection implementations
        if (objectClass.isAssignableFrom(List.class)) return (T) stream.collect(Collectors.toList());
        if (objectClass.isAssignableFrom(Set.class)) return (T) stream.collect(Collectors.toSet());

        // Convert to array
        if (objectClass.isArray()) {
            return (T) stream.toArray(size -> (T[]) Array.newInstance(objectClass.getComponentType(), size));
        }

        throw new IllegalArgumentException("Cannot recover stream from " + objectClass.getName());
    }

    /**
     * Converts an object to a Stream with optional parallel processing.
     *
     * @param <T>      the type of elements in the stream
     * @param value    the input object to convert
     * @param parallel true if the stream should be parallel
     * @return a Stream of type T
     */
    private static <T> Stream<T> toStream(Object value, boolean parallel) {
        // Handle Stream instances
        if (value instanceof Stream) {
            @SuppressWarnings("unchecked")
            Stream<T> castedStream = (Stream<T>) value;
            return castedStream;
        }

        // Handle Iterable instances
        if (value instanceof Iterable) {
            @SuppressWarnings("unchecked")
            Iterator<T> iterator = ((Iterable<T>) value).iterator();
            Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);

            return StreamSupport.stream(spliterator, parallel);
        }

        // Handle array instances
        if (value.getClass().isArray()) {
            @SuppressWarnings("unchecked")
            Stream<T> castedStream = Stream.of((T[]) value);
            return castedStream;
        }

        // Return empty stream for unsupported types
        return Stream.empty();
    }

    /**
     * Checks if the given element context represents a streamable type.
     *
     * @param elementContext the context containing the annotated element and its type
     * @return true if the type is streamable (Stream, Iterable, or array), false otherwise
     */
    private static boolean isStreamable(AnnotatedElementContext elementContext) {
        Class<?> unwrappedType = elementContext.getUnwrappedType();
        return Stream.class.isAssignableFrom(unwrappedType) ||
                Iterable.class.isAssignableFrom(unwrappedType) ||
                unwrappedType.isArray();
    }

    /**
     * Writes a stream to a PacketDataSerializer.
     *
     * @param <T>    the type of elements in the stream
     * @param writer the serializer to write to
     * @param stream the stream to serialize
     */
    public static <T> void writeStream(PacketDataSerializer writer, Stream<T> stream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(baos);
        AtomicInteger count = new AtomicInteger();

        try {
            // Write placeholder for element count
            dataOutput.writeLong(0L);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }

        // Serialize each element
        PacketDataSerializer internalSerializer = new PacketDataSerializer(dataOutput);
        stream.forEach(element -> {
            try {
                ClassSerializer.encode(
                        internalSerializer,
                        AnnotatedElementContext.ofClass(element)
                );
                count.incrementAndGet();
            } catch (PacketSerializationException e) {
                e.printStackTrace(System.out);
            }
        });

        // Update count and write data
        byte[] data = baos.toByteArray();
        ByteBuffer.wrap(data).putLong(0, count.longValue());
        writer.write(data);

        // Clean up stream
        stream.close();
    }

    /**
     * Reads a stream from a PacketDataSerializer.
     *
     * @param <T>         the type of elements in the stream
     * @param reader      the serializer to read from
     * @param streamClass the class of the stream elements
     * @return a Stream of type T
     * @throws PacketSerializationException if deserialization fails
     */
    public static <T> Stream<T> readStream(PacketDataSerializer reader, Class<T> streamClass) throws PacketSerializationException {
        long count = reader.readLong();
        Stream.Builder<T> builder = Stream.builder();

        // Deserialize each element
        for (int i = 0; i < count; i++) {
            builder.add(ClassSerializer.decode(reader, streamClass));
        }

        return builder.build();
    }

    /**
     * A codec for handling streamable types in packet serialization.
     *
     * @param <T> the type of the streamable object
     */
    static final class StreamCodec<T> extends PacketTypeCodec<T> {

        /**
         * Constructs a StreamCodec for the specified streamable type.
         *
         * @param streamableClass the class of the streamable object
         * @param streamType      the type of elements in the stream
         * @param parallel        true if parallel stream processing is enabled
         */
        public StreamCodec(Class<T> streamableClass, Class<?> streamType, boolean parallel) {
            super(
                    (writer, value) -> writeStream(writer, value, parallel),
                    reader -> readStream(reader, streamableClass, streamType)
            );
        }

        private static void writeStream(PacketDataSerializer writer, Object value, boolean parallel) {
            StreamableCodecResolver.writeStream(writer, StreamableCodecResolver.toStream(value, parallel));
        }

        private static <T> T readStream(PacketDataSerializer reader, Class<T> streamableClass, Class<?> streamType) throws PacketSerializationException {
            return StreamableCodecResolver.recoverStream(
                    StreamableCodecResolver.readStream(reader, streamType),
                    streamableClass
            );
        }
    }
}