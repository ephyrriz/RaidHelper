package ru.ephy.raidhelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This class is used only to highlight methods that might
 * be removed in the future. IMPLEMENTED TEMPORARY
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface MayBeRemoved {
    String value() default "This method might be removed in future versions.";
}
