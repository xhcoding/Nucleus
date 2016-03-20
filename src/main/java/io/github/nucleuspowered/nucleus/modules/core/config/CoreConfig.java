/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CoreConfig {

    @Setting(value = "debug-mode", comment = "loc:config.debugmode")
    private boolean debugmode = false;

    public boolean isDebugmode() {
        return debugmode;
    }
}
