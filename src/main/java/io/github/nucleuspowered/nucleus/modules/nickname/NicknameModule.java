/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname;

import io.github.nucleuspowered.nucleus.internal.StandardModule;
import io.github.nucleuspowered.nucleus.modules.nickname.config.NicknameConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.util.Optional;

@ModuleData(id = "nickname", name = "Nickname")
public class NicknameModule extends StandardModule {

    @Override
    public Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return Optional.of(new NicknameConfigAdapter());
    }
}
