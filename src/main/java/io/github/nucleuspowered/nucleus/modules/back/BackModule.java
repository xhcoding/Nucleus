/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.back;

import io.github.nucleuspowered.nucleus.api.service.NucleusBackService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.back.config.BackConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.back.handlers.BackHandler;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "back", name = "Back", softDependencies = "jail")
public class BackModule extends ConfigurableModule<BackConfigAdapter> {

    @Override
    public BackConfigAdapter createAdapter() {
        return new BackConfigAdapter();
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        BackHandler m = new BackHandler();
        serviceManager.registerService(BackHandler.class, m);
        Sponge.getServiceManager().setProvider(plugin, NucleusBackService.class, m);
    }
}
