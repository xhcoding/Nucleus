/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.blacklist.config;

import io.github.nucleuspowered.nucleus.Util;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Optional;

@ConfigSerializable
public class BlacklistConfig {

    @Setting(value = "use-replacement", comment = "config.blacklist.use-replacement")
    private boolean useReplacement = false;

    @Setting(comment = "config.blacklist.replacement")
    private String replacement = ItemTypes.DIRT.getId();

    @Setting(value = "blocked-actions", comment = "config.blacklist.blockedactions")
    private Types blockedActions = new Types();

    @ConfigSerializable
    public static class Types {

        @Setting(value = "possession", comment = "config.blacklist.possession")
        private boolean possession = true;

        @Setting(value = "environment", comment = "config.blacklist.environment")
        private boolean environment = true;

        @Setting(value = "use", comment = "config.blacklist.use")
        private boolean use = true;
    }

    public boolean shouldUseReplacement() {
        return useReplacement;
    }

    public CatalogType getReplacement() {
        return Util.getCatalogTypeForItemFromId(replacement).orElse(ItemTypes.DIRT);
    }

    public ItemStack getReplacementItemStack() {
        CatalogType type = getReplacement();
        if (type instanceof ItemType) {
            return ItemStack.of((ItemType)type, 1);
        }

        // Assume Block State
        try {
            BlockState bs = (BlockState) type;
            return ItemStack.builder().fromBlockState(bs).quantity(1).build();
        } catch (Exception e) {
            return ItemStack.of(ItemTypes.DIRT, 1);
        }
    }

    public Optional<ItemStackSnapshot> getItemStackIfShouldUseReplacement() {
        if (shouldUseReplacement()) {
            return Optional.of(getReplacementItemStack().createSnapshot());
        }

        return Optional.empty();
    }

    public boolean getPossession() {
        return blockedActions.possession;
    }

    public boolean getEnvironment() {
        return blockedActions.environment;
    }

    public boolean getUse() {
        return blockedActions.use;
    }
}
