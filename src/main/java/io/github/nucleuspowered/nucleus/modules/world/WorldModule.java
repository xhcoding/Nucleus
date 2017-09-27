/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.world.config.WorldConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = WorldModule.ID, name = "World")
public class WorldModule extends ConfigurableModule<WorldConfigAdapter> {

    public static final String ID = "world";

    @Override public WorldConfigAdapter createAdapter() {
        return new WorldConfigAdapter();
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        try {
            WorldHelper wh = new WorldHelper();
            plugin.registerReloadable(wh);
            serviceManager.registerService(WorldHelper.class, wh);
        } catch (Exception ex) {
            Nucleus.getNucleus().getLogger().warn("Could not load the world module for the reason below.");
            ex.printStackTrace();
            throw ex;
        }
    }
}
