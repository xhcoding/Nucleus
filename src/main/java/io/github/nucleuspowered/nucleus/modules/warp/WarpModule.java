/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import io.github.nucleuspowered.nucleus.config.WarpsConfig;
import io.github.nucleuspowered.nucleus.internal.ConfigMap;
import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfigAdapter;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.util.Optional;

@ModuleData(id = "warp", name = "Warp")
public class WarpModule extends StandardModule {

    @Inject private ConfigMap configMap;
    @Inject private Game game;
    @Inject private Logger logger;

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        try {
            configMap.putConfig(ConfigMap.WARPS_CONFIG, new WarpsConfig(nucleus.getDataPath().resolve("warp.json")));

            // Put the warp service into the service manager.
            game.getServiceManager().setProvider(nucleus, NucleusWarpService.class, configMap.getConfig(ConfigMap.WARPS_CONFIG).get());
        } catch (Exception ex) {
            logger.warn("Could not load the warp module for the reason below.");
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return Optional.of(new WarpConfigAdapter());
    }
}
