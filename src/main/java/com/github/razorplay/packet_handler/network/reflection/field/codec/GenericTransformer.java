package com.github.razorplay.packet_handler.network.reflection.field.codec;

import com.github.razorplay.packet_handler.network.reflection.field.GenericTypeAction;

/**
 * Functional interface representing a transformation from one {@link GenericTypeAction} to another,
 * potentially changing the action output type based on the input object.
 * <p>
 * This is useful when the encoding behavior needs to be dynamically altered or decorated
 * depending on the context of the input object, such as encoding only specific aspects
 * or adapting the output format.
 *
 * @param <T> the input object type that is being transformed.
 * @param <V> the output type that the resulting action will produce.
 */
@FunctionalInterface
public interface GenericTransformer<T, V, TAction extends GenericTypeAction<T>, VAction extends GenericTypeAction<V>> {

    /**
     * Transforms a given action into a new one for type {@code V},
     * optionally using the parameters to influence the transformation.
     *
     * @param object  the instance of {@code T} used as context for the transformation.
     * @param encoder the original action capable of encoding type {@code T}.
     * @return a new {@link GenericTypeAction} capable of encoding type {@code V}.
     */
    VAction transform(T object, TAction encoder);
}
