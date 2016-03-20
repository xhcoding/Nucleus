/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class AdminConfig {

    @Setting(comment = "loc:config.broadcast.tag")
    private String tag = "[Broadcast]";

    @Setting(comment = "loc:config.broadcast.msg")
    private String colorCode = "a";

    public String getTag() {
        return tag;
    }

    public String getColorCode() {
        return colorCode;
    }
}
