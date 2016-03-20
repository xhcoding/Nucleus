/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class NicknameConfigAdapter extends NucleusConfigAdapter<NicknameConfig> {

    @Override
    protected NicknameConfig getDefaultObject() {
        return new NicknameConfig();
    }

    @Override
    protected NicknameConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(NicknameConfig.class), new NicknameConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(NicknameConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(NicknameConfig.class), data);
    }
}
