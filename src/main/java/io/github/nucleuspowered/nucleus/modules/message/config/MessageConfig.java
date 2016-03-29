/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class MessageConfig {

    @Setting(value = "helpop-prefix", comment = "loc:config.message.helpop.prefix")
    private String helpOpPrefix = "&7HelpOp: {{name}} &7> &r";

    public String getHelpOpPrefix() {
        return helpOpPrefix;
    }
}
