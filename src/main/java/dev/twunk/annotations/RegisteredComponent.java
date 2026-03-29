package dev.twunk.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a component is annotated with this, i'll auto-register the relevant
 * systems to run its methods it defines (based on the interfaces it implements)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisteredComponent {}
