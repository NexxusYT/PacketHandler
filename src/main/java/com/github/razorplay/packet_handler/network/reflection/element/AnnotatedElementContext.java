package com.github.razorplay.packet_handler.network.reflection.element;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.reflection.util.ReflectionUtil;
import lombok.AllArgsConstructor;
import lombok.Value;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

@Value
@AllArgsConstructor
public class AnnotatedElementContext {

    AnnotatedElement annotatedElement;

    @Nullable
    Object value;
    Class<?> unwrappedType;

    public AnnotatedElementContext(AnnotatedElement annotatedElement, Class<?> unwrappedType) {
        this.annotatedElement = annotatedElement;
        this.unwrappedType = unwrappedType;

        this.value = null;
    }

    public static AnnotatedElementContext of(Field field) {
        return new AnnotatedElementContext(field, ReflectionUtil.unwrapBoxedType(field.getType()));
    }

    public static AnnotatedElementContext of(Field field, Object value) throws PacketSerializationException {
        Class<?> unwrappedType = ReflectionUtil.unwrapBoxedType(field.getType());
        try {
            field.setAccessible(true);

            Object fieldData = field.get(value);
            return new AnnotatedElementContext(field, fieldData, unwrappedType);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException exception) {
            throw new PacketSerializationException("Error while accessing field " + field.getName(), exception);
        }
    }

    public static AnnotatedElementContext of(Parameter parameter) {
        return new AnnotatedElementContext(parameter, ReflectionUtil.unwrapBoxedType(parameter.getType()));
    }

    public static AnnotatedElementContext of(Class<?> clazz) {
        return new AnnotatedElementContext(clazz, ReflectionUtil.unwrapBoxedType(clazz));
    }

    public static AnnotatedElementContext of(Class<?> clazz, Object value) {
        return new AnnotatedElementContext(clazz, value, ReflectionUtil.unwrapBoxedType(clazz));
    }

    public static AnnotatedElementContext ofClass(Object value) {
        return new AnnotatedElementContext(value.getClass(), value, ReflectionUtil.unwrapBoxedType(value.getClass()));
    }
}
