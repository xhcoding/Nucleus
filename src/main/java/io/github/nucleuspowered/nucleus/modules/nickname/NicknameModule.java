/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname;

import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.nickname.config.NicknameConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = NicknameModule.ID, name = "Nickname")
public class NicknameModule extends ConfigurableModule<NicknameConfigAdapter> {

    public final static String ID = "nickname";

    @Override
    public NicknameConfigAdapter createAdapter() {
        return new NicknameConfigAdapter();
    }
}
