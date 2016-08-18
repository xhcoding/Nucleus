/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class MiscConfigAdapter extends NucleusConfigAdapter<MiscConfig> {

    private final TypeToken<MiscConfig> miscConfigTypeToken = TypeToken.of(MiscConfig.class);

    @Override
    protected MiscConfig getDefaultObject() {
        return new MiscConfig();
    }

    @Override
    protected MiscConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(miscConfigTypeToken, getDefaultObject());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(MiscConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(miscConfigTypeToken, data);
    }
}
