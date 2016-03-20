/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.playerinfo.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ListConfig {

    @Setting(value = "group-by-permission-groups", comment = "loc:config.playerinfo.list.groups")
    private boolean groupByPermissionGroup = false;

    public boolean isGroupByPermissionGroup() {
        return groupByPermissionGroup;
    }
}
