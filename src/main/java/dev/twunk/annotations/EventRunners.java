package dev.twunk.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a class with this to have its codec auto-generated. Define properties
 * on this annotation to customise the behaviour
 */
public final class EventRunners {

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Chunk {
        Class<?>[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Entity {
        Class<?>[] value();
    }
}
