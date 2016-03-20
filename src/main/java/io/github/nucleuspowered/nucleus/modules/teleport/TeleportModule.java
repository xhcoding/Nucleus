/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport;

import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "teleport", name = "Teleport")
public class TeleportModule extends StandardModule {

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        serviceManager.registerService(TeleportHandler.class, new TeleportHandler(nucleus));
    }
}
