/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.back.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class BackConfigAdapter extends NucleusConfigAdapter<BackConfig> {

    @Override
    protected BackConfig getDefaultObject() {
        return new BackConfig();
    }

    @Override
    protected BackConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(BackConfig.class), new BackConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(BackConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(BackConfig.class), data);
    }
}
