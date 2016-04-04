/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jump;

import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.modules.jump.config.JumpConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.util.Optional;

@ModuleData(id = "jump", name = "Jump")
public class JumpModule extends StandardModule {

    @Override
    public Optional<AbstractConfigAdapter<?>> createConfigAdapter() {
        return Optional.of(new JumpConfigAdapter());
    }
}
