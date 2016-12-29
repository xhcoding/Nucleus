/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.exceptions;

import org.spongepowered.api.plugin.PluginContainer;

public class PluginAlreadyRegisteredException extends Exception {

    private final PluginContainer container;

    public PluginAlreadyRegisteredException(String message, PluginContainer container) {
        super(message);
        this.container = container;
    }

    /**
     * The {@link PluginContainer} that could not be registered.
     *
     * @return The {@link PluginContainer}
     */
    public PluginContainer getContainer() {
        return container;
    }
}
