/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.blacklist.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class BlacklistConfigAdapter extends NucleusConfigAdapter<BlacklistConfig> {
    @Override
    protected BlacklistConfig getDefaultObject() {
        return new BlacklistConfig();
    }

    @Override
    protected BlacklistConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(BlacklistConfig.class), new BlacklistConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(BlacklistConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(BlacklistConfig.class), data);
    }
}
