package io.github.nucleuspowered.nucleus.internal.annotations;

import java.lang.annotation.*;

/**
 * A marker class for the tests to signify that no permissions are required.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface NoPermissions {
}
