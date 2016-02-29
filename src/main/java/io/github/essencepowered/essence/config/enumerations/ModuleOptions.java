/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.config.enumerations;

public enum ModuleOptions {
    /**
     * Loads the module unless another plugin requests it to not be removed.
     */
    DEFAULT,

    /**
     * Does not load the module.
     */
    DISABLED,

    /**
     * Loads the module, even if a plugin asks for it to be removed.
     */
    FORCELOAD
}
