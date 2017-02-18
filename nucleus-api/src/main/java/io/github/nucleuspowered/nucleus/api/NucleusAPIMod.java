/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api;

import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;

@Plugin(id = NucleusAPI.ID, name = NucleusAPI.NAME, version = NucleusAPI.VERSION)
public final class NucleusAPIMod {

    private final Logger logger;

    @Inject
    public NucleusAPIMod(Logger logger) {
        this.logger = logger;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        logger.info("Loading " + NucleusAPI.NAME + " for Nucleus version " + NucleusAPI.VERSION);
        NucleusAPI.onPreInit(this);
    }
}
