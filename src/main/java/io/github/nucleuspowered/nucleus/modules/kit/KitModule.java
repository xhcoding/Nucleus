/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.service.NucleusKitService;
import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.util.Optional;

@ModuleData(id = "kit", name = "Kit")
public class KitModule extends StandardModule {

    @Inject private Game game;
    @Inject private Logger logger;

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        try {
            KitHandler kitHandler = new KitHandler();
            nucleus.getInjector().injectMembers(kitHandler);
            serviceManager.registerService(KitHandler.class, kitHandler);
            game.getServiceManager().setProvider(nucleus, NucleusKitService.class, kitHandler);
        } catch (Exception ex) {
            logger.warn("Could not load the kits module for the reason below.");
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public Optional<AbstractConfigAdapter<?>> createConfigAdapter() {
        return Optional.of(new KitConfigAdapter());
    }
}
