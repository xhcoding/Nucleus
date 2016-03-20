/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.playerinfo;

import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.modules.playerinfo.config.PlayerInfoConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.util.Optional;

@ModuleData(id = "playerinfo", name = "Player Info")
public class PlayerInfoModule extends StandardModule {

    @Override
    public Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return Optional.of(new PlayerInfoConfigAdapter());
    }
}
