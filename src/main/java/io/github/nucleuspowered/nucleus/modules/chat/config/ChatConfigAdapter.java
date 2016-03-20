/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class ChatConfigAdapter extends NucleusConfigAdapter<ChatConfig> {
    @Override
    protected ChatConfig getDefaultObject() {
        return new ChatConfig();
    }

    @Override
    protected ChatConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(ChatConfig.class));
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(ChatConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(ChatConfig.class), data);
    }
}
