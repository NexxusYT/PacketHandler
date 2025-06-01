package com.github.razorplay.packet_handler.network.reflection.element;

@FunctionalInterface
public interface ElementPredicate {

    boolean matches(AnnotatedElementContext context);
}
