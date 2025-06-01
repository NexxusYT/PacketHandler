package com.github.razorplay.packet_handler.network.reflection.element.codec;

import com.github.razorplay.packet_handler.network.reflection.element.AnnotatedElementContext;
import com.github.razorplay.packet_handler.network.reflection.element.ElementPredicate;
import com.github.razorplay.packet_handler.network.reflection.element.codec.type.PacketTypeCodec;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@AllArgsConstructor
public class PrioritizedCodecResolver<T> {

    private final int priority;
    private final CodecResolver<T> codecResolver;
    private final ElementPredicate elementPredicate;

    public PrioritizedCodecResolver(CodecResolver<T> codecResolver, ElementPredicate elementPredicate) {
        this.priority = 0;

        this.codecResolver = codecResolver;
        this.elementPredicate = elementPredicate;
    }

    public static PrioritizedCodecResolver<?>[] sort(PrioritizedCodecResolver<?>[] resolvers) {
        for (int i = 0; i < resolvers.length; i++) {
            for (int j = i + 1; j < resolvers.length; j++) {
                if (resolvers[i].priority < resolvers[j].priority) {
                    PrioritizedCodecResolver<?> temp = resolvers[i];
                    resolvers[i] = resolvers[j];
                    resolvers[j] = temp;
                }
            }
        }

        return resolvers;
    }

    public boolean matches(AnnotatedElementContext context) {
        return elementPredicate.matches(context);
    }

    public PacketTypeCodec<T> resolveCodec(AnnotatedElementContext context) {
        return codecResolver.resolveCodec(context);
    }
}
