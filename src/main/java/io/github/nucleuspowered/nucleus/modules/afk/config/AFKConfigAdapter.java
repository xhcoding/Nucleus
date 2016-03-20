/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class AFKConfigAdapter extends NucleusConfigAdapter<AFKConfig> {
    @Override
    protected AFKConfig getDefaultObject() {
        return new AFKConfig();
    }

    @Override
    protected AFKConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(AFKConfig.class));
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(AFKConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(AFKConfig.class), data);
    }
}
