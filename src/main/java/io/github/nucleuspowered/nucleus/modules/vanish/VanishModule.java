/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish;

import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.vanish.config.VanishConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.vanish.service.VanishService;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "vanish", name = "Vanish")
public class VanishModule extends ConfigurableModule<VanishConfigAdapter> {

    @Override
    public VanishConfigAdapter createAdapter() {
        return new VanishConfigAdapter();
    }

    @Override
    protected void performPreTasks() throws Exception {
        VanishService vs = new VanishService();
        this.plugin.getInternalServiceManager().registerService(VanishService.class, vs);
        this.plugin.registerReloadable(vs);
        super.performPreTasks();
    }
}
