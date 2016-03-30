/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.annotations;

import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;

import java.lang.annotation.*;

/**
 * Specifies that the class is a command and that the command loader should register it at startup.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RegisterCommand {

    /**
     * The subcommand that this represents. Defaults to the {@link AbstractCommand} class.
     *
     * @return The subcommand.
     */
    Class<? extends AbstractCommand> subcommandOf() default AbstractCommand.class;

    /**
     * The aliases for this command.
     *
     * @return Aliases for this command.
     */
    String[] value();
}
