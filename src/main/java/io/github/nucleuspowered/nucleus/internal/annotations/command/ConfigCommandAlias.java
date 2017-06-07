/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.annotations.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signifies that the name of the command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ConfigCommandAlias {
    /**
     * The command name to use in the config file.
     *
     * @return The name.
     */
    String value();

    /**
     * Whether to ask the system to generate the default config.
     *
     * @return <code>true</code> if so, <code>false</code> otherwise.
     */
    boolean generate() default true;
}
