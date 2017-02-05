/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.modular;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.UUID;

public class ModularWorldService extends ModularDataService<ModularWorldService> {

    private final UUID worldUUID;
    private final NucleusPlugin plugin;

    public ModularWorldService(DataProvider<ConfigurationNode> dataProvider, NucleusPlugin plugin, UUID worldUUID) throws Exception {
        super(dataProvider);
        this.worldUUID = worldUUID;
        this.plugin = plugin;
    }

    public UUID getWorldUUID() {
        return this.worldUUID;
    }
}
