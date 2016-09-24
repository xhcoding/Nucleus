/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.annotations;

import java.lang.annotation.*;

/**
 * Indicates that the Nucleus Mixin plugin is required for this command/listener/event to be loaded.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireMixinPlugin {

    /**
     * Returns whether to document this requirement in DocGen.
     *
     * @return {@code true} if so.
     */
    boolean document() default true;

    /**
     * Returns whether the console is notified on startup that this class may be skipped.
     *
     * @return {@code true} if so.
     */
    boolean notifyOnLoad() default true;

    /**
     * Sets whether this command should only be loaded when the Mixin Companion plugin is present, not, or in both caes.
     * Defaults to both.
     *
     * @return {@link MixinLoad} that defines whether to load the command or not.
     */
    MixinLoad value();

    enum MixinLoad {
        BOTH,
        NO_MIXIN,
        MIXIN_ONLY
    }
}
