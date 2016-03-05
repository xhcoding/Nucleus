/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.annotations;

import io.github.nucleuspowered.nucleus.api.PluginModule;

import java.lang.annotation.*;

/**
 * An annotation to specify what modules a command or a listener belongs to.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Modules {
    /**
     * The modules that this command or listener is a part of.
     *
     * @return An array of {@link PluginModule}s.
     */
    PluginModule[] value() default { };
}
