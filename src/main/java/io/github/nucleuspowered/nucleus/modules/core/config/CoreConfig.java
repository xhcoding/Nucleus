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

    @Setting(value = "use-custom-message-file", comment = "loc:config.custommessages")
    private boolean custommessages = false;

    @Setting(value = "permission-command", comment = "loc:config.permissioncommand")
    private String permissionCommand = "";

    @Setting(value = "warmup-canceling", comment = "loc:config.core.warmup.info")
    private WarmupConfig warmupConfig = new WarmupConfig();

    public boolean isDebugmode() {
        return debugmode;
    }

    public boolean isCustommessages() {
        return custommessages;
    }

    public String getPermissionCommand() {
        return permissionCommand;
    }

    public WarmupConfig getWarmupConfig() {
        return warmupConfig;
    }
}
