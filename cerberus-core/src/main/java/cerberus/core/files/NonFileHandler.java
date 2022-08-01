package cerberus.core.files;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simply used to mark a FileManager (super classes of) as one that does not
 * handle files This is generally used for database cleanup tasks or others that
 * need a timing mechanism to fire off of. Use reflection to determine if a
 * class is annotated.
 * 
 * NO LONGER USED - just kept around for reference
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NonFileHandler {
}
