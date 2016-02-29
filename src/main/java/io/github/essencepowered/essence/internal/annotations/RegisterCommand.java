/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.internal.annotations;

import io.github.essencepowered.essence.internal.CommandBase;

import java.lang.annotation.*;

/**
 * Specifies that the class is a command and that the command loader should register it at startup.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RegisterCommand {

    /**
     * The subcommand that this represents. Defaults to the {@link CommandBase} class.
     *
     * @return The subcommand.
     */
    Class<? extends CommandBase> subcommandOf() default CommandBase.class;

    /**
     * The aliases for this command.
     *
     * @return Aliases for this command.
     */
    String[] value();
}
