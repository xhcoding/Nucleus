/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.commandlogger.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class CommandLoggerConfigAdapter extends NucleusConfigAdapter<CommandLoggerConfig> {

    @Override
    protected CommandLoggerConfig getDefaultObject() {
        return new CommandLoggerConfig();
    }

    @Override
    protected CommandLoggerConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(CommandLoggerConfig.class), new CommandLoggerConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(CommandLoggerConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(CommandLoggerConfig.class), data);
    }
}
