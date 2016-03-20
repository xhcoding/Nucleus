/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class KitConfigAdapter extends NucleusConfigAdapter<KitConfig> {
    @Override
    protected KitConfig getDefaultObject() {
        return new KitConfig();
    }

    @Override
    protected KitConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(KitConfig.class), new KitConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(KitConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(KitConfig.class), data);
    }
}
