package com.github.razorplay.packet_handler.network.reflection.element.codec;

import com.github.razorplay.packet_handler.network.reflection.element.AnnotatedElementContext;
import com.github.razorplay.packet_handler.network.reflection.element.ElementPredicate;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * A wrapper around a {@link CodecResolver} that includes a priority and a predicate
 * to determine whether the resolver is applicable for a context.
 *
 * <p>Resolvers with higher priority values are considered first when sorted.</p>
 */
@EqualsAndHashCode
@AllArgsConstructor
public class PrioritizedCodecResolver {

    private final int priority;
    private final CodecResolver codecResolver;
    private final ElementPredicate elementPredicate;

    /**
     * Creates a {@code PrioritizedCodecResolver} with a default priority of 0.
     *
     * @param codecResolver    the codec resolver.
     * @param elementPredicate the predicate that determines applicability.
     */
    public PrioritizedCodecResolver(CodecResolver codecResolver, ElementPredicate elementPredicate) {
        this.priority = 0;
        this.codecResolver = codecResolver;
        this.elementPredicate = elementPredicate;
    }

    /**
     * Sorts an array of {@code PrioritizedCodecResolver} instances in descending order of priority.
     *
     * @param resolvers the array of resolvers to sort.
     * @return the sorted array, with the highest-priority resolvers first.
     */
    public static PrioritizedCodecResolver[] sort(PrioritizedCodecResolver[] resolvers) {
        for (int i = 0; i < resolvers.length; i++) {
            for (int j = i + 1; j < resolvers.length; j++) {
                if (resolvers[i].priority < resolvers[j].priority) {
                    PrioritizedCodecResolver temp = resolvers[i];
                    resolvers[i] = resolvers[j];
                    resolvers[j] = temp;
                }
            }
        }
        return resolvers;
    }

    /**
     * Checks whether this resolver matches the given context.
     *
     * @param context the annotation context to evaluate.
     * @return {@code true} if the predicate matches, {@code false} otherwise.
     */
    public boolean matches(AnnotatedElementContext context) {
        return elementPredicate.matches(context);
    }

    /**
     * Resolves the codec using the internal {@link CodecResolver} based on the given context.
     *
     * @param context the annotation context to use during resolution.
     * @return the resolved {@link PacketTypeCodec}.
     */
    @SuppressWarnings("unchecked")
    public <T> PacketTypeCodec<T> resolveCodec(AnnotatedElementContext context) {
        return (PacketTypeCodec<T>) codecResolver.resolveTransformedCodec(context);
    }
}
