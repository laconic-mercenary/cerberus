package cerberus.core.tasks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// simply a marker to indicate that 
// a tasking can be 
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AsyncEligible {
	// WARNING: all Types that use this annotation MUST implement
	// Serializable
}
