/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config.serialisers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.api.data.Kit;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.time.Duration;
import java.util.List;

@ConfigSerializable
public class KitNode implements Kit {

    @Setting private List<ItemStackSnapshot> stacks;

    /**
     * This is in seconds to be consistent with the rest of the plugin.
     */
    @Setting private long interval;

    @Setting private double cost;

    public KitNode() {
        this.stacks = Lists.newArrayList();
        this.interval = 0;
        this.cost = 0;
    }

    public KitNode(Player player) {
        updateKitInventory(player);
        this.interval = 0;
        this.cost = 0;
    }

    public KitNode(List<ItemStackSnapshot> slots) {
        this.stacks = slots;
        this.interval = 0;
        this.cost = 0;
    }

    public KitNode(List<ItemStackSnapshot> slots, Duration interval) {
        this.stacks = slots;
        this.interval = interval.getSeconds();
        this.cost = 0;
    }

    @Override public List<ItemStackSnapshot> getStacks() {
        return stacks;
    }

    @Override public Kit setStacks(List<ItemStackSnapshot> stacks) {
        this.stacks = stacks;
        return this;
    }

    @Override public Duration getInterval() {
        return Duration.ofSeconds(interval);
    }

    @Override public Kit setInterval(Duration interval) {
        this.interval = interval.getSeconds();
        return this;
    }

    @Override public double getCost() {
        return this.cost;
    }

    @Override public Kit setCost(double cost) {
        this.cost = cost;
        return this;
    }
}
