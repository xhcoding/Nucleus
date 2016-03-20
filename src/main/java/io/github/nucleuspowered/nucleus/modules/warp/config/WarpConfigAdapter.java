/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class WarpConfigAdapter extends NucleusConfigAdapter<WarpConfig> {
    @Override
    protected WarpConfig getDefaultObject() {
        return new WarpConfig();
    }

    @Override
    protected WarpConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(WarpConfig.class));
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(WarpConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(WarpConfig.class), data);
    }
}
