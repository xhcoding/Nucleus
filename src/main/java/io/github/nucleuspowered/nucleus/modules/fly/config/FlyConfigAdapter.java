/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.fly.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class FlyConfigAdapter extends NucleusConfigAdapter<FlyConfig> {

    private TypeToken<FlyConfig> tt = TypeToken.of(FlyConfig.class);

    @Override
    protected FlyConfig getDefaultObject() {
        return new FlyConfig();
    }

    @Override
    protected FlyConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(tt, getDefaultObject());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(FlyConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(tt, data);
    }
}
