package com.github.razorplay.packet_handler.network.reflection;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.CustomSerializable;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.reflection.element.AnnotatedElementContext;
import com.github.razorplay.packet_handler.network.reflection.element.BuiltInCodecs;
import com.github.razorplay.packet_handler.network.reflection.element.codec.PrioritizedCodecResolver;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

public class ClassSerializer {

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

    public static <T> T tryDecodeWithCodecs(PacketDataSerializer reader, AnnotatedElementContext context) throws PacketSerializationException {
        Class<?> output = context.getUnwrappedType();
        if (CustomSerializable.class.isAssignableFrom(output)) {
            return tryDecodeFromCustomSerializable(reader, context);
        }

        System.out.println("Searching specific codecs...");

        try {
            PacketTypeCodec<T> codec = getCodec(context);
            return codec.getReader().decode(reader);
        } catch (PacketSerializationException e) {
            throw new PacketSerializationException("No codec found for " + output, e);
        }
    }

    public static boolean tryEncodeWithCodecs(PacketDataSerializer writer, AnnotatedElementContext context) {
        Object input = context.getValue();

        if (input instanceof CustomSerializable) {
            CustomSerializable customSerializable = (CustomSerializable) input;
            customSerializable.serialize(writer);
            return true;
        }

        System.out.println("Searching specific codecs...");

        try {
            PacketTypeCodec<Object> codec = getCodec(context);
            codec.getWriter().encode(writer, input);
            return true;
        } catch (PacketSerializationException ignored) {
        }

        return false;
    }

    public static <T> T decode(PacketDataSerializer reader, Class<T> output) throws PacketSerializationException {
        AnnotatedElementContext context = AnnotatedElementContext.of(output);
        try {
            return ClassSerializer.tryDecodeWithCodecs(reader, context);
        } catch (PacketSerializationException exception) {
            exception.printStackTrace(System.out);
        }

        System.out.println("Creating custom codec:");

        Constructor<T> emptyConstructor = null;
        Constructor<T> fullConstructor = null;

        Field[] fields = output.getDeclaredFields();
        Constructor<T>[] constructors = (Constructor<T>[]) output.getConstructors();

        int l = 0;
        for (Field field : fields) {
            if (!field.isSynthetic()) {
                l++;
            }
        }
        System.out.println("Found " + l + " declared fields.");
        for (Constructor<T> constructor : constructors) {
            System.out.println("Found constructor with " + constructor.getParameterCount() + " parameters.");

            if (constructor.getParameterCount() == l) fullConstructor = constructor;
            if (constructor.getParameterCount() == 0) emptyConstructor = constructor;

            if (emptyConstructor != null && fullConstructor != null) break;
        }
        if (emptyConstructor == null && fullConstructor == null) {
            throw new PacketSerializationException("No default constructor found for custom serializable");
        }

        if (fullConstructor != null) {
            Parameter[] parameters = fullConstructor.getParameters();
            Object[] instances = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                context = AnnotatedElementContext.of(parameters[i]);
                try {
                    instances[i] = ClassSerializer.tryDecodeWithCodecs(reader, context);
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

        T out;
        try {
            out = emptyConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new PacketSerializationException("Failed to instantiate custom serializable", e);
        }

        for (Field field : fields) {
            if (field.isSynthetic()) continue;

            context = AnnotatedElementContext.of(field);
            try {
                field.setAccessible(true);
                field.set(out, ClassSerializer.tryDecodeWithCodecs(reader, context));
            } catch (IllegalAccessException e) {
                e.printStackTrace(System.out);
            }
        }
        return out;
    }

    public static boolean encode(PacketDataSerializer writer, AnnotatedElementContext context) throws PacketSerializationException {
        if (ClassSerializer.tryEncodeWithCodecs(writer, context)) {
            return true;
        }

        System.out.println("Creating custom codec:");

        Object input = context.getValue();
        AnnotatedElementContext elementContext;

        for (Field field : context.getUnwrappedType().getDeclaredFields()) {
            if (field.isSynthetic()) continue;

            elementContext = AnnotatedElementContext.of(field, input);
            ClassSerializer.tryEncodeWithCodecs(writer, elementContext);
        }
        return false;
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
}
