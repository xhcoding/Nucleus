/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config.serialisers;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.ItemType;

import java.util.List;

@ConfigSerializable
public class GeneralDataNode {

    @Setting
    private List<ItemType> blacklistedTypes = Lists.newArrayList();

    public List<ItemType> getBlacklistedTypes() {
        return blacklistedTypes;
    }

    public void setBlacklistedTypes(List<ItemType> blacklistedTypes) {
        this.blacklistedTypes = blacklistedTypes;
    }
}
