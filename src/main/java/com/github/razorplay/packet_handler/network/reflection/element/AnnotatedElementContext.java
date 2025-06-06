package com.github.razorplay.packet_handler.network.reflection.element;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.reflection.util.ReflectionUtil;
import lombok.AllArgsConstructor;
import lombok.Value;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * Encapsulates metadata about an annotated element (such as a field, parameter, or class),
 * its runtime value (if available), and its unwrapped type (e.g., converting boxed types to primitives).
 *
 * <p>This context is used to guide codec resolution based on reflection and type analysis.</p>
 */
@Value
@AllArgsConstructor
public class AnnotatedElementContext {

    AnnotatedElement annotatedElement;

    @Nullable
    Object value;
    Class<?> unwrappedType;

    /**
     * Constructs a context for an element without a known runtime value.
     *
     * @param annotatedElement the annotated element.
     * @param unwrappedType    the unwrapped type of the element.
     */
    public AnnotatedElementContext(AnnotatedElement annotatedElement, Class<?> unwrappedType) {
        this.annotatedElement = annotatedElement;
        this.unwrappedType = unwrappedType;
        this.value = null;
    }

    /**
     * Creates a context for a field without accessing its value.
     *
     * @param field the field to wrap.
     * @return a new {@link AnnotatedElementContext} with the unwrapped type.
     */
    public static AnnotatedElementContext of(Field field) {
        return new AnnotatedElementContext(field, ReflectionUtil.unwrapBoxedType(field.getType()));
    }

    /**
     * Creates a context for a field and retrieves its value from the provided instance.
     *
     * @param field the field to wrap.
     * @param value the instance from which to retrieve the field's value.
     * @return a new {@link AnnotatedElementContext} containing the field's value and type.
     * @throws PacketSerializationException if the field value cannot be accessed.
     */
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

    /**
     * Creates a context for a method or constructor parameter.
     *
     * @param parameter the parameter to wrap.
     * @return a new {@link AnnotatedElementContext} with the unwrapped type.
     */
    public static AnnotatedElementContext of(Parameter parameter) {
        return new AnnotatedElementContext(parameter, ReflectionUtil.unwrapBoxedType(parameter.getType()));
    }

    /**
     * Creates a context for a class type (no instance).
     *
     * @param clazz the class to wrap.
     * @return a new {@link AnnotatedElementContext} with the unwrapped class type.
     */
    public static AnnotatedElementContext of(Class<?> clazz) {
        return new AnnotatedElementContext(clazz, ReflectionUtil.unwrapBoxedType(clazz));
    }

    /**
     * Creates a context for a class type with an associated value.
     *
     * @param clazz the class to wrap.
     * @param value the associated instance value.
     * @return a new {@link AnnotatedElementContext}.
     */
    public static AnnotatedElementContext of(Class<?> clazz, Object value) {
        return new AnnotatedElementContext(clazz, value, ReflectionUtil.unwrapBoxedType(clazz));
    }

    /**
     * Creates a context directly from a runtime object.
     *
     * @param value the runtime object.
     * @return a new {@link AnnotatedElementContext} based on the object's class.
     */
    public static AnnotatedElementContext ofClass(Object value) {
        return new AnnotatedElementContext(value.getClass(), value, ReflectionUtil.unwrapBoxedType(value.getClass()));
    }
}