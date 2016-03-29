/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class MessageConfigAdapter extends NucleusConfigAdapter<MessageConfig> {

    @Override
    protected MessageConfig getDefaultObject() {
        return new MessageConfig();
    }

    @Override
    protected MessageConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(MessageConfig.class), new MessageConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(MessageConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(MessageConfig.class), data);
    }
}
