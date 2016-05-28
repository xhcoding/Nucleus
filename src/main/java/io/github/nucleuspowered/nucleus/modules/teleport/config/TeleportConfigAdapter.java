/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class TeleportConfigAdapter extends NucleusConfigAdapter<TeleportConfig> {

    private final TypeToken<TeleportConfig> ttc = TypeToken.of(TeleportConfig.class);

    @Override
    protected TeleportConfig getDefaultObject() {
        return new TeleportConfig();
    }

    @Override
    protected TeleportConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(ttc, new TeleportConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(TeleportConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(ttc, data);
    }
}
