/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk;

import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.util.Optional;

@ModuleData(id = "afk", name = "AFK")
public class AFKModule extends StandardModule {

    @Override
    public Optional<AbstractConfigAdapter<?>> createConfigAdapter() {
        return Optional.of(new AFKConfigAdapter());
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        serviceManager.registerService(AFKHandler.class, new AFKHandler());
    }
}
