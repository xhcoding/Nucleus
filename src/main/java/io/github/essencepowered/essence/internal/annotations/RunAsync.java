/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.internal.annotations;

import io.github.essencepowered.essence.internal.CommandBase;

import java.lang.annotation.*;

/**
 * Any {@link CommandBase} that is decorated with this annotation will be
 * run on an async thread. This should only be used for thread-safe operations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RunAsync {
}
