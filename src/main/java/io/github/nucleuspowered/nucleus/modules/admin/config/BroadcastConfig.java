/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.config;

import io.github.nucleuspowered.nucleus.configurate.annotations.Default;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplate;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class BroadcastConfig {

    @Setting
    @Default(value = "&a[Broadcast] ", saveDefaultIfNull = true)
    private NucleusTextTemplate prefix;

    @Setting
    @Default(value = "", saveDefaultIfNull = true)
    private NucleusTextTemplate suffix;

    public NucleusTextTemplate getPrefix() {
        return prefix;
    }

    public NucleusTextTemplate getSuffix() {
        return suffix;
    }
}
