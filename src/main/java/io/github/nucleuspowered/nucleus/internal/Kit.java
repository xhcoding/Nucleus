/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;

@ConfigSerializable
public class Kit {

    @Setting private List<ItemStack> stacks;

    @Setting private long interval;

    public Kit() {
        this.stacks = Lists.newArrayList();
        this.interval = 0;
    }

    public Kit(List<ItemStack> slots) {
        this.stacks = slots;
        this.interval = 0;
    }

    public Kit(List<ItemStack> slots, long interval) {
        this.stacks = slots;
        this.interval = interval;
    }

    public List<ItemStack> getStacks() {
        return stacks;
    }

    public Kit setStacks(List<ItemStack> stacks) {
        this.stacks = stacks;
        return this;
    }

    public long getInterval() {
        return interval;
    }

    public Kit setInterval(long interval) {
        this.interval = interval;
        return this;
    }
}
