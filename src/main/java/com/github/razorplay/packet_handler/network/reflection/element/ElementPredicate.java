package com.github.razorplay.packet_handler.network.reflection.element;

/**
 * Functional interface representing a predicate that tests whether a given
 * {@link AnnotatedElementContext} matches specific criteria.
 *
 * <p>This is typically used to determine whether a resolver should
 * be applied to a context based on annotations, types, or other metadata.</p>
 *
 * <pre><code>
 * ElementPredicate stringTypePredicate = context -&gt;
 *     context.getUnwrappedType().equals(String.class);
 * </code></pre>
 */
@FunctionalInterface
public interface ElementPredicate {

    /**
     * Evaluates whether the given context matches the criteria defined by this predicate.
     *
     * @param context the {@link AnnotatedElementContext} to evaluate.
     * @return {@code true} if the context matches; {@code false} otherwise.
     */
    boolean matches(AnnotatedElementContext context);
}
