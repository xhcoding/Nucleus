/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.annotations;

import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;

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
     * Replaces the command portion of the permission with the specified alias - "prefix.alias.base"
     *
     * @return The name of the alias to use
     */
    String alias() default "";

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
     * The suggested permission level.
     *
     * @return The {@link SuggestedLevel}
     */
    SuggestedLevel suggestedLevel() default SuggestedLevel.ADMIN;
}
