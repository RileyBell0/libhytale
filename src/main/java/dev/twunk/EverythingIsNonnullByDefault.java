package dev.twunk;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifierDefault;

/**
 * Sets pretty much everything i can get my hands on to be @Nonnull
 *
 * Exceptions include (list may change as i discover more)
 * - Generics. These don't count as @Nonnull unless explicitly annotated
 */
@Documented
@TypeQualifierDefault({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EverythingIsNonnullByDefault {}
