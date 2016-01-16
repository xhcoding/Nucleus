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

    /**
     * Arbitrary permissions that could be checked.
     *
     * @return The permissions
     */
    String[] value() default {};

    /**
     * Use the default permission - "prefix.(root).command.{sub}.base"
     *
     * @return <code>true</code> if this permission should be used.
     */
    boolean useDefault() default true;

    /**
     * Additional permissions for cooldown exemption.
     *
     * @return The list of permissions
     */
    String[] cooldownExempt() default {};

    /**
     * Additional permissions for warmup exemption.
     *
     * @return The list of permissions
     */
    String[] warmupExempt() default {};

    /**
     * Use the default permission - "prefix.(root).command.{sub}.exempt.cooldown"
     *
     * @return <code>true</code> if this permission should be used.
     */
    boolean useDefaultCooldownExempt() default true;

    /**
     * Use the default permission - "prefix.(root).command.{sub}.exempt.warmup"
     *
     * @return <code>true</code> if this permission should be used.
     */
    boolean useDefaultWarmupExempt() default true;

    /**
     * The root permission to use
     *
     * @return The root, or empty string if no root
     */
    String root() default "";

    /**
     * The sub permission to use
     *
     * @return The sub, or empty string if no root
     */
    String sub() default "";

    /**
     * Include the admin permission in this check.
     *
     * @return <code>true</code> if the admin permission is also permissible.
     */
    boolean includeAdmin() default true;
}
