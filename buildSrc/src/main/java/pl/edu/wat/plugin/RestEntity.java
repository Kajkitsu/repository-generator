package pl.edu.wat.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for entity classes that require automatic generation of
 * Spring Data REST repositories. Classes marked with this annotation
 * will have a corresponding repository interface created at build time.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RestEntity {
}