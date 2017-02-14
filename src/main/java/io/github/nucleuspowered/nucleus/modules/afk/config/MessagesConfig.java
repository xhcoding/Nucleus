/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.config;

import io.github.nucleuspowered.nucleus.configurate.annotations.Default;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplate;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class MessagesConfig {

    @Setting("on-afk")
    @Default(value = "&7*&f{{displayname}} &7has gone AFK.", saveDefaultIfNull = true)
    private NucleusTextTemplate afkMessage;

    @Setting("on-return")
    @Default(value = "&7*&f{{displayname}} &7is no longer AFK.", saveDefaultIfNull = true)
    private NucleusTextTemplate returnAfkMessage;

    @Setting("on-command")
    @Default(value = "&f{{displayname}} &7is currently AFK and may not respond quickly.", saveDefaultIfNull = true)
    private NucleusTextTemplate onCommand;

    @Setting(value = "on-kick", comment = "config.afk.messagetobroadcastonkick")
    @Default(value = "&f{{displayname}} &7has been kicked for being AFK too long.", saveDefaultIfNull = true)
    private NucleusTextTemplate onKick;

    @Setting(value = "kick-message-to-subject", comment = "config.afk.playerkicked")
    @Default(value = "You have been kicked for being AFK for too long.", saveDefaultIfNull = true)
    private NucleusTextTemplate kickMessage;

    public NucleusTextTemplate getAfkMessage() {
        return afkMessage;
    }

    public NucleusTextTemplate getReturnAfkMessage() {
        return returnAfkMessage;
    }

    public NucleusTextTemplate getOnCommand() {
        return onCommand;
    }

    public NucleusTextTemplate getOnKick() {
        return onKick;
    }

    public NucleusTextTemplate getKickMessage() {
        return kickMessage;
    }
}
