/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.List;

public class CoreConfigAdapter extends NucleusConfigAdapter<CoreConfig> {

    @Override
    protected List<Transformation> getTransformations() {
        List<Transformation> lt = super.getTransformations();

        // Delete the "permission-command" node as we do not need it any more.
        lt.add(new Transformation(new Object[] { "permission-command" }, (inputPath, valueAtPath) -> {
            valueAtPath.setValue(null);
            return null;
        }));

        return lt;
    }

    @Override
    protected CoreConfig getDefaultObject() {
        return new CoreConfig();
    }

    @Override
    protected CoreConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(TypeToken.of(CoreConfig.class));
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(CoreConfig data) throws ObjectMappingException {
        return SimpleCommentedConfigurationNode.root().setValue(TypeToken.of(CoreConfig.class), data);
    }
}
