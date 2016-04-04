/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.warp.handlers.WarpHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.util.Optional;

@ModuleData(id = "warp", name = "Warp")
public class WarpModule extends StandardModule {

    @Inject private Game game;
    @Inject private Logger logger;

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        try {
            // Put the warp service into the service manager.

            WarpHandler wh = new WarpHandler();
            nucleus.getInjector().injectMembers(wh);
            serviceManager.registerService(WarpHandler.class, wh);
            game.getServiceManager().setProvider(nucleus, NucleusWarpService.class, wh);
        } catch (Exception ex) {
            logger.warn("Could not load the warp module for the reason below.");
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public Optional<AbstractConfigAdapter<?>> createConfigAdapter() {
        return Optional.of(new WarpConfigAdapter());
    }
}
