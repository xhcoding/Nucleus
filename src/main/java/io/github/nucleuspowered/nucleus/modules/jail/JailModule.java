/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusJailService;
import io.github.nucleuspowered.nucleus.config.WarpsConfig;
import io.github.nucleuspowered.nucleus.internal.ConfigMap;
import io.github.nucleuspowered.nucleus.internal.InternalServiceManager;
import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.util.Optional;

@ModuleData(id = "jail", name = "Jail")
public class JailModule extends StandardModule {

    @Inject private Nucleus nucleus;
    @Inject private ConfigMap configMap;
    @Inject private Logger logger;
    @Inject private Game game;
    @Inject private InternalServiceManager serviceManager;

    @Override
    public Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return Optional.of(new JailConfigAdapter());
    }

    @Override
    protected void performPreTasks() throws Exception {
        try {
            configMap.putConfig(ConfigMap.JAILS_CONFIG, new WarpsConfig(nucleus.getDataPath().resolve("jails.json")));

            JailHandler jh = new JailHandler(nucleus);
            game.getServiceManager().setProvider(nucleus, NucleusJailService.class, jh);
            serviceManager.registerService(JailHandler.class, jh);
        } catch (Exception ex) {
            logger.warn("Could not load the jail module for the reason below.");
            ex.printStackTrace();
            throw ex;
        }
    }
}
