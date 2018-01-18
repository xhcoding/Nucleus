/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish;

import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.vanish.config.VanishConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.vanish.service.VanishService;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@RegisterService(VanishService.class)
@ModuleData(id = "vanish", name = "Vanish")
public class VanishModule extends ConfigurableModule<VanishConfigAdapter> {

    @Override
    public VanishConfigAdapter createAdapter() {
        return new VanishConfigAdapter();
    }

}
