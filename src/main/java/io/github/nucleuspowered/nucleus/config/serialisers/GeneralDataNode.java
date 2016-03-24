/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config.serialisers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.api.data.JailData;
import io.github.nucleuspowered.nucleus.api.data.Kit;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.ItemType;

import java.util.List;
import java.util.Map;

@ConfigSerializable
public class GeneralDataNode {

    @Setting
    private List<ItemType> blacklistedTypes = Lists.newArrayList();

    @Setting
    private Map<String, LocationNode> jails = Maps.newHashMap();

    @Setting
    private Map<String, LocationNode> warps = Maps.newHashMap();

    @Setting
    private Map<String, KitDataNode> kits = Maps.newHashMap();

    public List<ItemType> getBlacklistedTypes() {
        return blacklistedTypes;
    }

    public void setBlacklistedTypes(List<ItemType> blacklistedTypes) {
        this.blacklistedTypes = blacklistedTypes;
    }

    public Map<String, LocationNode> getJails() {
        return jails;
    }

    public Map<String, LocationNode> getWarps() {
        return warps;
    }

    public Map<String, KitDataNode> getKits() {
        return kits;
    }
}
