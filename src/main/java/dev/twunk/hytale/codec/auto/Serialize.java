package dev.twunk.hytale.codec.auto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Serialize {
    /**
     * If the parameter is required in the CODEC to be valid
     */
    boolean required() default false;

    /**
     * The string name (must start with an Uppercase Letter) under which to
     * store the value in the CODEC. Defaults to the same name as the variable
     * itself with an uppercase letter
     *
     * e.g. java variable named `myField` will be stored in the codec under "MyField": <>
     */
    String key() default "";

    /**
     * Distinquish whether this is specifically a chunk or entity store related item.
     * Needed for finding codecs of elements within. Notably, common components are added
     * to both stores so you're welcome to leave this as true for something that JUST uses
     * common components OR for something that uses a mix of common components and chunk
     * components
     */
    boolean inChunkStore() default true;

    /**
     * IFF the variable this is annotating is a short/int, i'll ensure it is
     * greater than or equal to the value put in here
     *
     * If it's not a short/int, i'm not going to check anything.
     */
    int min() default Integer.MIN_VALUE;
}
