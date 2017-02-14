/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.config;

import io.github.nucleuspowered.nucleus.configurate.annotations.Default;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplate;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ChatTemplateConfig {

    @Setting(value = "default-chatcolour", comment = "config.chat.template.chatcolour")
    private String chatcolour = "";

    @Setting(value = "default-chatstyle", comment = "config.chat.template.chatstyle")
    private String chatstyle = "";

    @Setting(value = "default-namecolour", comment = "config.chat.template.namecolour")
    private String namecolour = "";

    @Setting(value = "default-namestyle", comment = "config.chat.template.namestyle")
    private String namestyle = "";

    @Setting(comment = "config.chat.template.prefix")
    @Default(value = "{{prefix}} {{displayname}}{{suffix}}&f: ", saveDefaultIfNull = true)
    private NucleusTextTemplate prefix;

    @Setting(comment = "config.chat.template.suffix")
    private NucleusTextTemplate suffix = NucleusTextTemplate.Empty.INSTANCE;

    public NucleusTextTemplate getPrefix() {
        return prefix;
    }

    public NucleusTextTemplate getSuffix() {
        return suffix;
    }

    public String getChatcolour() {
        return chatcolour;
    }

    public String getChatstyle() {
        return chatstyle;
    }

    public String getNamecolour() {
        return namecolour;
    }

    public String getNamestyle() {
        return namestyle;
    }
}
