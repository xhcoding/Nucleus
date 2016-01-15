package uk.co.drnaylor.minecraft.quickstart.internal.annotations;

import java.lang.annotation.*;

/**
 * Specifies multiple permissions that this command could use. Be sure that no permissions have been set in the
 * {@link org.spongepowered.api.command.spec.CommandSpec.Builder}.
 *
 * <p>By default, using this annotation will also allow the command to be run by those with the admin permission.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Permissions {
    String[] value();
    boolean includeAdmin() default true;
}
