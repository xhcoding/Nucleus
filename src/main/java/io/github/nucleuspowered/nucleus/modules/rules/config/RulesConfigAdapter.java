/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rules.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class RulesConfigAdapter extends NucleusConfigAdapter<RulesConfig> {

    @Override
    protected RulesConfig getDefaultObject() {
        return new RulesConfig();
    }

    @Override
    protected RulesConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(RulesConfig.class), new RulesConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(RulesConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(RulesConfig.class), data);
    }
}
