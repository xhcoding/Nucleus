/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.datatypes;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.api.data.Kit;
import io.github.nucleuspowered.nucleus.configurate.typeserialisers.ItemStackSnapshotSerialiser;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.time.Duration;
import java.util.List;

@ConfigSerializable
public class KitDataNode implements Kit {

    private static final TypeToken<ItemStackSnapshot> tt = new TypeToken<ItemStackSnapshot>() {};

    @Setting private List<ConfigurationNode> stacks = Lists.newArrayList();

    /**
     * This is in seconds to be consistent with the rest of the plugin.
     */
    @Setting private long interval = 0;

    @Setting private double cost = 0;

    @Setting private boolean oneTime = false;

    @Override
    public List<ItemStackSnapshot> getStacks() {
        List<ItemStackSnapshot> lcn = Lists.newArrayList();
        for (ConfigurationNode cn : stacks) {
            try {
                lcn.add(ItemStackSnapshotSerialiser.INSTANCE.deserialize(tt, cn));
            } catch (ObjectMappingException e) {
                //
            }
        }

        return lcn;
    }

    @Override
    public Kit setStacks(List<ItemStackSnapshot> stacks) {
        List<ConfigurationNode> lcn = Lists.newArrayList();
        for (ItemStackSnapshot stackSnapshot : stacks) {
            ConfigurationNode cn = SimpleConfigurationNode.root();
            try {
                ItemStackSnapshotSerialiser.INSTANCE.serialize(tt, stackSnapshot, cn);
                lcn.add(cn);
            } catch (ObjectMappingException e) {
                //
            }
        }

        this.stacks = lcn;
        return this;
    }

    @Override
    public Duration getInterval() {
        return Duration.ofSeconds(interval);
    }

    @Override
    public Kit setInterval(Duration interval) {
        this.interval = interval.getSeconds();
        return this;
    }

    @Override
    public double getCost() {
        return this.cost;
    }

    @Override
    public Kit setCost(double cost) {
        this.cost = cost;
        return this;
    }

    @Override
    public boolean isOneTime() {
        return this.oneTime;
    }

    @Override
    public Kit setOneTime(boolean oneTime) {
        this.oneTime = oneTime;
        return this;
    }
}
