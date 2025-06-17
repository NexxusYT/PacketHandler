package com.github.razorplay.packet_handler.network.reflection;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.CustomSerializable;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.reflection.element.AnnotatedElementContext;
import com.github.razorplay.packet_handler.network.reflection.element.BuiltInCodecs;
import com.github.razorplay.packet_handler.network.reflection.element.codec.PrioritizedCodecResolver;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility class for serializing and deserializing objects to/from a {@link PacketDataSerializer}.
 * Supports custom serialization via {@link CustomSerializable}, codec-based serialization using
 * {@link PacketTypeCodec}, and reflective field and constructor-based serialization for complex types.
 * Handles circular references and ensures proper type resolution through {@link AnnotatedElementContext}.
 */
public final class ClassSerializer {

    /**
     * Attempts to decode an object from a {@link PacketDataSerializer} using a default constructor
     * for types implementing {@link CustomSerializable}.
     *
     * @param <T>     the type of the object to decode
     * @param reader  the serializer to read data from
     * @param context the context containing type and annotation information
     * @return the decoded object
     * @throws PacketSerializationException if no default constructor is found or instantiation fails
     */
    @SuppressWarnings("unchecked")
    public static <T> T tryDecodeFromCustomSerializable(PacketDataSerializer reader, AnnotatedElementContext context) throws PacketSerializationException {
        Constructor<?>[] constructors = context.getUnwrappedType().getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                constructor.setAccessible(true);
                try {
                    CustomSerializable customSerializable = (CustomSerializable) constructor.newInstance();
                    customSerializable.deserialize(reader);
                    return (T) customSerializable;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new PacketSerializationException("Failed to instantiate custom serializable", e);
                }
            }
        }
        throw new PacketSerializationException("No default constructor found for custom serializable. There must be a constructor with any parameters.");
    }

    /**
     * Recursively decodes an object from a {@link PacketDataSerializer} using codecs or reflective
     * instantiation, checking for circular references.
     *
     * @param <T>        the type of the object to decode
     * @param reader     the serializer to read data from
     * @param context    the context containing type and annotation information
     * @param inputCache a list of class hash codes to detect circular references
     * @return the decoded object
     * @throws PacketSerializationException if a circular reference is detected, no codec is found,
     *                                      or instantiation fails
     */
    @SuppressWarnings("unchecked")
    public static <T> T tryDecodeWithCodecsRecursively(PacketDataSerializer reader, AnnotatedElementContext context, List<Integer> inputCache) throws PacketSerializationException {
        Class<T> output = (Class<T>) context.getUnwrappedType();

        if (inputCache.contains(output.hashCode())) {
            throw new PacketSerializationException("Circular reference detected for " + output);
        }

        if (CustomSerializable.class.isAssignableFrom(output)) {
            return tryDecodeFromCustomSerializable(reader, context);
        }

        PacketTypeCodec<T> codec = getCodec(context);
        if (codec != null) {
            return codec.getReader().decode(reader);
        }

        inputCache.add(output.hashCode());
        return ClassSerializer.createAndPopulateInstance(reader, output, inputCache);
    }

    /**
     * Recursively encodes an object to a {@link PacketDataSerializer} using codecs or reflective
     * field access, checking for circular references.
     *
     * @param writer     the serializer to write data to
     * @param context    the context containing the object, type, and annotation information
     * @param inputCache a list of class hash codes to detect circular references
     * @return true if encoding was successful, false otherwise
     * @throws PacketSerializationException if a circular reference is detected or encoding fails
     */
    public static boolean tryEncodeWithCodecsRecursively(PacketDataSerializer writer, AnnotatedElementContext context, List<Integer> inputCache) throws PacketSerializationException {
        Object input = context.getValue();
        AnnotatedElementContext elementContext = context;
        if (inputCache.contains(elementContext.getUnwrappedType().hashCode())) {
            throw new PacketSerializationException("Circular reference detected for " + elementContext.getUnwrappedType());
        }

        if (input instanceof CustomSerializable) {
            CustomSerializable customSerializable = (CustomSerializable) input;
            customSerializable.serialize(writer);
            return true;
        }

        PacketTypeCodec<Object> codec = getCodec(elementContext);
        if (codec != null) {
            codec.getWriter().encode(writer, input);
            return true;
        }

        inputCache.add(elementContext.getUnwrappedType().hashCode());
        for (Field field : elementContext.getUnwrappedType().getDeclaredFields()) {
            if (!ClassSerializer.isFieldValid(field)) continue;

            elementContext = AnnotatedElementContext.of(field, input);
            ClassSerializer.tryEncodeWithCodecsRecursively(writer, elementContext, inputCache);
        }
        return false;
    }

    /**
     * Creates an instance using a constructor matching the number of fields and populates its parameters.
     *
     * @param <T>             the type of the object to create
     * @param fullConstructor the constructor to use
     * @param reader          the serializer to read data from
     * @param inputCache      a list of class hash codes to detect circular references
     * @return the created and populated instance
     * @throws PacketSerializationException if parameter decoding or instantiation fails
     */
    private static <T> T createFullConstructor(Constructor<T> fullConstructor, PacketDataSerializer reader, List<Integer> inputCache) throws PacketSerializationException {
        Parameter[] parameters = fullConstructor.getParameters();
        Object[] instances = new Object[parameters.length];

        AnnotatedElementContext context;
        for (int i = 0; i < parameters.length; i++) {
            try {
                context = AnnotatedElementContext.of(parameters[i]);
                instances[i] = ClassSerializer.tryDecodeWithCodecsRecursively(reader, context, inputCache);
            } catch (PacketSerializationException e) {
                throw new PacketSerializationException("Failed to decode parameter " + i + " " + parameters[i].getType().getName(), e);
            }
        }

        try {
            fullConstructor.setAccessible(true);
            return fullConstructor.newInstance(instances);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new PacketSerializationException("Failed to instantiate custom serializable", e);
        }
    }

    /**
     * Creates an instance using a default constructor and populates its fields via reflection.
     *
     * @param <T>        the type of the object to create
     * @param reader     the serializer to read data from
     * @param output     the class to instantiate
     * @param inputCache a list of class hash codes to detect circular references
     * @return the created and populated instance
     * @throws PacketSerializationException if no default constructor is found, instantiation fails,
     *                                      or field population fails
     */
    private static <T> T createAndPopulateInstance(PacketDataSerializer reader, Class<T> output, List<Integer> inputCache) throws PacketSerializationException {
        Field[] fields = Stream.of(output.getDeclaredFields()).filter(ClassSerializer::isFieldValid).toArray(Field[]::new);

        Constructor<T>[] constructors = ClassSerializer.getConstructors(output);
        Constructor<T> emptyConstructor = null;

        for (Constructor<T> constructor : constructors) {
            if (constructor.getParameterCount() == fields.length) {
                try {
                    return ClassSerializer.createFullConstructor(constructor, reader, inputCache);
                } catch (PacketSerializationException ignored) {
                }
            }

            if (constructor.getParameterCount() == 0) {
                emptyConstructor = constructor;
            }
        }

        if (emptyConstructor == null) {
            throw new PacketSerializationException("No default constructor found for custom serializable");
        }

        T out;
        try {
            out = emptyConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new PacketSerializationException("Failed to instantiate custom serializable", e);
        }

        AnnotatedElementContext elementContext;
        for (Field field : fields) {
            elementContext = AnnotatedElementContext.of(field);
            try {
                field.setAccessible(true);
                field.set(out, ClassSerializer.tryDecodeWithCodecsRecursively(reader, elementContext, inputCache));
            } catch (IllegalAccessException e) {
                e.printStackTrace(System.out);
            }
        }
        return out;
    }

    /**
     * Decodes an object of the specified type from a {@link PacketDataSerializer}.
     *
     * @param <T>    the type of the object to decode
     * @param reader the serializer to read data from
     * @param output the class to decode into
     * @return the decoded object
     * @throws PacketSerializationException if decoding fails due to missing codecs, circular references,
     *                                      or instantiation issues
     */
    public static <T> T decode(PacketDataSerializer reader, Class<T> output) throws PacketSerializationException {
        return ClassSerializer.tryDecodeWithCodecsRecursively(reader, AnnotatedElementContext.of(output), new ArrayList<>());
    }

    /**
     * Encodes an object to a {@link PacketDataSerializer} using the provided context.
     *
     * @param writer  the serializer to write data to
     * @param context the context containing the object, type, and annotation information
     * @return true if encoding was successful, false otherwise
     * @throws PacketSerializationException if encoding fails due to circular references or missing codecs
     */
    public static boolean encode(PacketDataSerializer writer, AnnotatedElementContext context) throws PacketSerializationException {
        return ClassSerializer.tryEncodeWithCodecsRecursively(writer, context, new ArrayList<>());
    }

    /**
     * Resolves a codec for the given context to handle serialization or deserialization.
     *
     * @param <T>     the type of the object handled by the codec
     * @param context the context containing type and annotation information
     * @return the resolved codec
     * @throws PacketSerializationException if no codec is found or resolution fails
     */
    @Nullable
    public static <T> PacketTypeCodec<T> getCodec(AnnotatedElementContext context) throws PacketSerializationException {
        Class<?> inputClass = context.getUnwrappedType();

        for (PrioritizedCodecResolver resolver : BuiltInCodecs.SORTED_RESOLVERS) {
            if (resolver.matches(context)) {
                try {
                    return resolver.resolveCodec(context);
                } catch (Exception exception) {
                    throw new PacketSerializationException("Failed to resolve codec for " + inputClass, exception);
                }
            }
        }
        return null;
    }

    /**
     * Checks if a field is valid for serialization (non-synthetic and non-static).
     *
     * @param field the field to check
     * @return true if the field is valid, false otherwise
     */
    private static boolean isFieldValid(Field field) {
        return !field.isSynthetic() && !Modifier.isStatic(field.getModifiers());
    }

    /**
     * Retrieves all public constructors for the given class.
     *
     * @param <T>   the type of the class
     * @param clazz the class to retrieve constructors for
     * @return an array of public constructors
     */
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T>[] getConstructors(Class<T> clazz) {
        return (Constructor<T>[]) clazz.getConstructors();
    }
}