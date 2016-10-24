/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.datatypes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.ItemType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

@ConfigSerializable
public class GeneralDataNode {

    @Setting
    private List<ItemType> blacklistedTypes = Lists.newArrayList();

    @Setting
    private Map<String, LocationNode> jails = Maps.newHashMap();

    @Setting
    private Map<String, WarpNode> warps = Maps.newHashMap();

    @Setting
    @Nullable
    private LocationNode firstspawn = null;

    public List<ItemType> getBlacklistedTypes() {
        if (this.blacklistedTypes == null) {
            this.blacklistedTypes = Lists.newArrayList();
        }

        return this.blacklistedTypes;
    }

    public void setBlacklistedTypes(List<ItemType> blacklistedTypes) {
        if (blacklistedTypes == null) {
            this.blacklistedTypes = Lists.newArrayList();
        } else {
            this.blacklistedTypes = blacklistedTypes;
        }
    }

    public Map<String, LocationNode> getJails() {
        return jails;
    }

    public Map<String, WarpNode> getWarps() {
        return warps;
    }

    public Optional<LocationNode> getFirstSpawnLocation() {
        return Optional.ofNullable(firstspawn);
    }

    public void setFirstSpawnLocation(@Nullable LocationNode node) {
        firstspawn = node;
    }

}
