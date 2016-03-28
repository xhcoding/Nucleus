/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mob.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class MobConfigAdapter extends NucleusConfigAdapter<MobConfig> {

    @Override
    protected MobConfig getDefaultObject() {
        return new MobConfig();
    }

    @Override
    protected MobConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(MobConfig.class), new MobConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(MobConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(MobConfig.class), data);
    }
}
