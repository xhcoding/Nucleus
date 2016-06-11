/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.fly.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class FlyConfig {

    @Setting(value = "save-all-flystate-on-quit", comment = "loc:config.fly.stateonquit")
    private boolean saveOnQuit = true;

    public boolean isSaveOnQuit() {
        return saveOnQuit;
    }
}
