/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusJailService;
import io.github.nucleuspowered.nucleus.internal.InternalServiceManager;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "jail", name = "Jail")
public class JailModule extends ConfigurableModule<JailConfigAdapter> {

    @Inject private Nucleus nucleus;
    @Inject private Logger logger;
    @Inject private Game game;
    @Inject private InternalServiceManager serviceManager;

    @Override
    public JailConfigAdapter getAdapter() {
        return new JailConfigAdapter();
    }

    @Override
    protected void performPreTasks() throws Exception {
        try {
            JailHandler jh = new JailHandler(nucleus);
            nucleus.getInjector().injectMembers(jh);
            game.getServiceManager().setProvider(nucleus, NucleusJailService.class, jh);
            serviceManager.registerService(JailHandler.class, jh);
        } catch (Exception ex) {
            logger.warn("Could not load the jail module for the reason below.");
            ex.printStackTrace();
            throw ex;
        }
    }
}
