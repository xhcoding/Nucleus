/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusKitService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = KitModule.ID, name = "Kit")
public class KitModule extends ConfigurableModule<KitConfigAdapter> {

    public static final String ID = "kit";

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        try {
            KitHandler kitHandler = new KitHandler();
            plugin.registerReloadable(kitHandler::reload);
            serviceManager.registerService(KitHandler.class, kitHandler);
            Sponge.getServiceManager().setProvider(plugin, NucleusKitService.class, kitHandler);
        } catch (Exception ex) {
            Nucleus.getNucleus().getLogger().warn("Could not load the kits module for the reason below.");
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public KitConfigAdapter createAdapter() {
        return new KitConfigAdapter();
    }
}
