/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.blacklist.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;

@ConfigSerializable
public class BlacklistConfig {

    @Setting(comment = "loc:config.blacklist.environment")
    private boolean environment = true;

    @Setting(comment = "loc:config.blacklist.inventory")
    private boolean inventory = true;
    
    @Setting(comment = "loc:config.blacklist.use-replacement")
    private boolean useReplacement = false;

    @Setting(comment = "loc:config.blacklist.replacement")
    private ItemType replacement = ItemTypes.DIRT;

    public boolean isEnvironment() {
        return environment;
    }

    public boolean isInventory() {
        return inventory;
    }

    public boolean shouldUseReplacement() {
        return useReplacement;
    }

    public ItemType getReplacement() {
        return replacement;
    }
}
