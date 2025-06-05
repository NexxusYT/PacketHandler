package com.github.razorplay.packet_handler.network.reflection;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.CustomSerializable;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.reflection.element.AnnotatedElementContext;
import com.github.razorplay.packet_handler.network.reflection.element.BuiltInCodecs;
import com.github.razorplay.packet_handler.network.reflection.element.codec.PrioritizedCodecResolver;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * x) Hacer que el encode y decode sean recursivos, con un array de parámetro para prevenir dependencias circulares.
 * -) Hacer un pequeño cache para las funciones que se pueda, especialmente la de getCodec.
 * -) Hacer un refactor a esta clase separando mejor las funciones etc.
 * -) Añadir javadocs al resto de las clases.
 * -) Y tienes pensado hacer una especie de @PacketHandler y PacketListener además de la clase SimplePacket que auto serialize.
 */
public final class ClassSerializer {

    @SuppressWarnings("unchecked")
    public static <T> T tryDecodeFromCustomSerializable(PacketDataSerializer reader, AnnotatedElementContext context) throws PacketSerializationException {
        Constructor<?>[] constructors = context.getUnwrappedType().getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                try {
                    CustomSerializable customSerializable = (CustomSerializable) constructor.newInstance();
                    customSerializable.deserialize(reader);
                    return (T) customSerializable;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new PacketSerializationException("Failed to instantiate custom serializable", e);
                }
            }
        }
        System.out.println("Exception triggered!");
        throw new PacketSerializationException("No default constructor found for custom serializable");
    }

    @SuppressWarnings("unchecked")
    public static <T> T tryDecodeWithCodecsRecursively(PacketDataSerializer reader, AnnotatedElementContext context, List<Integer> inputCache) throws PacketSerializationException {
        Class<T> output = (Class<T>) context.getUnwrappedType();
        if (inputCache.contains(output.hashCode())) {
            throw new PacketSerializationException("Circular reference detected for " + output);
        }

        if (CustomSerializable.class.isAssignableFrom(output)) {
            return tryDecodeFromCustomSerializable(reader, context);
        }

        try {
            PacketTypeCodec<T> codec = getCodec(context);
            return codec.getReader().decode(reader);
        } catch (PacketSerializationException ignored) {
        }

        inputCache.add(output.hashCode());
        return ClassSerializer.createAndPopulateInstance(reader, output, inputCache);
    }

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

        try {
            PacketTypeCodec<Object> codec = getCodec(elementContext);
            codec.getWriter().encode(writer, input);
            return true;
        } catch (PacketSerializationException ignored) {
        }

        inputCache.add(elementContext.getUnwrappedType().hashCode());
        for (Field field : elementContext.getUnwrappedType().getDeclaredFields()) {
            if (!ClassSerializer.isFieldValid(field)) continue;

            elementContext = AnnotatedElementContext.of(field, input);
            ClassSerializer.tryEncodeWithCodecsRecursively(writer, elementContext, inputCache);
        }
        return false;
    }

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

    private static <T> T createAndPopulateInstance(PacketDataSerializer reader, Class<T> output, List<Integer> inputCache) throws PacketSerializationException {
        Field[] fields = Stream.of(output.getDeclaredFields()).filter(ClassSerializer::isFieldValid).toArray(Field[]::new);

        Constructor<T>[] constructors = ClassSerializer.getConstructors(output);
        Constructor<T> emptyConstructor = null;

        for (Constructor<T> constructor : constructors) {
            System.out.println("Found constructor with " + constructor.getParameterCount() + " parameters.");

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

    public static <T> T decode(PacketDataSerializer reader, Class<T> output) throws PacketSerializationException {
        return ClassSerializer.tryDecodeWithCodecsRecursively(reader, AnnotatedElementContext.of(output), new ArrayList<>());
    }

    public static boolean encode(PacketDataSerializer writer, AnnotatedElementContext context) throws PacketSerializationException {
        return ClassSerializer.tryEncodeWithCodecsRecursively(writer, context, new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public static <T> PacketTypeCodec<T> getCodec(AnnotatedElementContext context) throws PacketSerializationException {
        Class<?> inputClass = context.getUnwrappedType();

        for (PrioritizedCodecResolver<?> resolver : BuiltInCodecs.SORTED_RESOLVERS) {
            if (resolver.matches(context)) {
                try {
                    return (PacketTypeCodec<T>) resolver.resolveCodec(context);
                } catch (Exception exception) {
                    throw new PacketSerializationException("Failed to resolve codec for " + inputClass, exception);
                }
            }
        }
        throw new PacketSerializationException("No codec found for " + inputClass);
    }

    private static boolean isFieldValid(Field field) {
        return !field.isSynthetic() && !Modifier.isStatic(field.getModifiers());
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T>[] getConstructors(Class<T> clazz) {
        return (Constructor<T>[]) clazz.getConstructors();
    }
}
