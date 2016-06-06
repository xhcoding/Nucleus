/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class SpawnConfigAdapter extends NucleusConfigAdapter<SpawnConfig> {

    private static TypeToken<SpawnConfig> tt = TypeToken.of(SpawnConfig.class);

    @Override
    protected SpawnConfig getDefaultObject() {
        return new SpawnConfig();
    }

    @Override
    protected SpawnConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(tt, getDefaultObject());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(SpawnConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(tt, data);
    }
}
