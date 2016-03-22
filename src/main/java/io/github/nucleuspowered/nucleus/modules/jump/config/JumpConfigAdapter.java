/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jump.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

public class JumpConfigAdapter extends AbstractConfigAdapter<JumpConfig> {

    @Override
    protected ConfigurationNode generateDefaults(ConfigurationNode node) {
        try {
            return node.setValue(TypeToken.of(JumpConfig.class), new JumpConfig());
        } catch (ObjectMappingException e) {
            return node;
        }
    }

    @Override
    protected JumpConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(JumpConfig.class), new JumpConfig());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(JumpConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(JumpConfig.class), new JumpConfig());
    }
}
