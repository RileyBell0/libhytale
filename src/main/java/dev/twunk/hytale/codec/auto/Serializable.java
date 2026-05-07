package dev.twunk.hytale.codec.auto;

import javax.lang.model.type.NullType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a class with this to have its codec auto-generated. Define properties
 * on this annotation to customise the behaviour
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Serializable {
    /// If this should EXTEND another codec (inherit another) then put the class in here
    /// that the codec is stored in.
    ///
    /// Note: you can put in a class of another @Serializable class in here and i'll
    /// extend THAT class' generated codec
    Class<?> inherits() default NullType.class;

    /// name of the static field in which the codec is stored. OK for this to fail. You
    /// DON'T need to put a BuilderCodec field on your class. Really if you want the auto-generation
    /// make sure this fails. Set it as an empty string or something.
    ///
    /// Defaults to "CODEC" since hytale seems to name all their codecs "CODEC" and
    /// makes them static within any given class.
    ///
    /// Really helptul to have, means i don't have to define codecs for stuff like Vector3i etc
    String codecField() default "CODEC";

    /**
     * Optional string to add as documentation to the overall codec
     */
    String documentation() default "";
}
