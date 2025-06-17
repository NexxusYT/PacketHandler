package com.github.razorplay.packet_handler.network.packet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark fields that can be processed as streams in serialization.<br>
 * This annotation specifies the class type to be used for streaming operations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Streamable {

    /**
     * Specifies the class type to be used for streaming operations.
     *
     * @return the class type associated with the streamable field
     */
    Class<?> value();
}