/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.service.NucleusMuteService;
import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.mute.config.MuteConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

import java.util.Optional;

@ModuleData(id = "mute", name = "Mute")
public class MuteModule extends StandardModule {

    @Inject private Game game;
    @Inject private Logger logger;

    @Override
    public Optional<NucleusConfigAdapter<?>> createConfigAdapter() {
        return Optional.of(new MuteConfigAdapter());
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        try {
            MuteHandler m = new MuteHandler(nucleus);
            nucleus.getInjector().injectMembers(m);
            game.getServiceManager().setProvider(nucleus, NucleusMuteService.class, m);
            serviceManager.registerService(MuteHandler.class, m);
        } catch (Exception ex) {
            logger.warn("Could not load the mute module for the reason below.");
            ex.printStackTrace();
            throw ex;
        }
    }
}
