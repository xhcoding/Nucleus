/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class MuteConfigAdapter extends NucleusConfigAdapter<MuteConfig> {
    @Override
    protected MuteConfig getDefaultObject() {
        return new MuteConfig();
    }

    @Override
    protected MuteConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(MuteConfig.class), new MuteConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(MuteConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(MuteConfig.class), data);
    }
}
