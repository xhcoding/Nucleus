/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.servershop.config;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class ServerShopConfigAdapter extends NucleusConfigAdapter<ServerShopConfig> {

    private final TypeToken<ServerShopConfig> tt = TypeToken.of(ServerShopConfig.class);

    @Override
    protected ServerShopConfig getDefaultObject() {
        return new ServerShopConfig();
    }

    @Override
    protected ServerShopConfig convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
        return node.getValue(tt, getDefaultObject());
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(ServerShopConfig data) throws ObjectMappingException {
        return null;
    }
}
