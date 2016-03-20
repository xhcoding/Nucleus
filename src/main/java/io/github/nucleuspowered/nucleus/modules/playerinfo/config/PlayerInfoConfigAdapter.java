/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.playerinfo.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class PlayerInfoConfigAdapter extends NucleusConfigAdapter<PlayerInfoConfig> {
    @Override
    protected PlayerInfoConfig getDefaultObject() {
        return new PlayerInfoConfig();
    }

    @Override
    protected PlayerInfoConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(PlayerInfoConfig.class), new PlayerInfoConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(PlayerInfoConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(PlayerInfoConfig.class), data);
    }
}
