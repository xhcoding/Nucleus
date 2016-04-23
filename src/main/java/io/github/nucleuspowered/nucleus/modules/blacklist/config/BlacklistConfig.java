/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.blacklist.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class BlacklistConfig {

    @Setting(comment = "loc:config.blacklist.environment")
    private boolean environment = true;

    @Setting(comment = "loc:config.blacklist.inventory")
    private boolean inventory = true;

    public boolean isEnvironment() {
        return environment;
    }

    public boolean isInventory() {
        return inventory;
    }
}
