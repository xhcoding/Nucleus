/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config.serialisers;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.inventory.ItemStack;

import java.time.Duration;
import java.util.List;

@ConfigSerializable
public class Kit {

    @Setting private List<ItemStack> stacks;

    /**
     * This is in seconds to be consistent with the rest of the plugin.
     */
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

    public Duration getInterval() {
        return Duration.ofSeconds(interval);
    }

    public Kit setInterval(Duration interval) {
        this.interval = interval.getSeconds();
        return this;
    }
}
