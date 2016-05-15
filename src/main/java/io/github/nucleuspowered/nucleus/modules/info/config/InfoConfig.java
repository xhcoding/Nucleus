/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.info.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class InfoConfig {

    @Setting(value = "show-motd-on-join", comment = "loc:config.motd.onjoin")
    private boolean showMotdOnJoin = true;

    public boolean isShowMotdOnJoin() {
        return showMotdOnJoin;
    }
}
