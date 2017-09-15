/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.modular;

import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Optional;
import java.util.UUID;

public class ModularWorldService extends ModularDataService<ModularWorldService> {

    private final UUID worldUUID;

    public ModularWorldService(DataProvider<ConfigurationNode> dataProvider, UUID worldUUID) throws Exception {
        super(dataProvider);
        this.worldUUID = worldUUID;
    }

    public UUID getWorldUUID() {
        return this.worldUUID;
    }

    @Override <T extends TransientModule<ModularWorldService>> Optional<T> tryGetTransient(Class<T> module) {
        return DataModuleFactory.getTransient(module, this);
    }

    @Override <T extends DataModule<ModularWorldService>> Optional<T> tryGet(Class<T> module) {
        return DataModuleFactory.get(module, this);
    }
}
