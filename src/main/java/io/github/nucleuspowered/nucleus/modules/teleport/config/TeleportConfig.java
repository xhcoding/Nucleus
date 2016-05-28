/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class TeleportConfig {

    @Setting(value = "default-quiet", comment = "loc:config.teleport.quiet")
    private boolean defaultQuiet = true;

    public boolean isDefaultQuiet() {
        return defaultQuiet;
    }
}
