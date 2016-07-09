/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rtp.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class RTPConfigAdapter extends NucleusConfigAdapter<RTPConfig> {

    private final TypeToken<RTPConfig> tt = TypeToken.of(RTPConfig.class);

    @Override
    protected RTPConfig getDefaultObject() {
        return new RTPConfig();
    }

    @Override
    protected RTPConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(tt, getDefaultObject());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(RTPConfig data) throws ObjectMappingException {
        return getNewNode().setValue(tt, data);
    }
}
