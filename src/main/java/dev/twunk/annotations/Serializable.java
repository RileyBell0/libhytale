package dev.twunk.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.lang.model.type.NullType;

/**
 * When a component is annotated with this, i'll auto-register the relevant
 * systems to run its methods it defines (based on the interfaces it implements)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Serializable {
    Class<?> inherits() default NullType.class;

    // name of the static field in which the codec is stored. OK for this to fail
    // - your class DOES NOT need to store this
    String codecField() default "CODEC";
}
