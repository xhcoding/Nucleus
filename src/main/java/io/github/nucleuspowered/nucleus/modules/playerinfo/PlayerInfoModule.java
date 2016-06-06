/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.playerinfo;

import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.playerinfo.config.PlayerInfoConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "playerinfo", name = "Player Info")
public class PlayerInfoModule extends ConfigurableModule<PlayerInfoConfigAdapter> {

    @Override
    public PlayerInfoConfigAdapter getAdapter() {
        return new PlayerInfoConfigAdapter();
    }
}
