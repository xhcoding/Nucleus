/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.connectionmessages.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class ConnectionMessagesConfigAdapter extends NucleusConfigAdapter<ConnectionMessagesConfig> {
    @Override
    protected ConnectionMessagesConfig getDefaultObject() {
        return new ConnectionMessagesConfig();
    }

    @Override
    protected ConnectionMessagesConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(ConnectionMessagesConfig.class), new ConnectionMessagesConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(ConnectionMessagesConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(ConnectionMessagesConfig.class), data);
    }
}
