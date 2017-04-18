/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.serverlist.config;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.neutrino.annotations.Default;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateImpl;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import uk.co.drnaylor.quickstart.config.NoMergeIfPresent;

import java.util.List;

@ConfigSerializable
public class ServerListConfig {

    @Setting(value = "modify-server-list-messages", comment = "config.serverlist.modify")
    private boolean modifyServerList = false;

    @Setting(value = "hide-vanished-players", comment = "config.serverlist.hidevanished")
    private boolean hideVanishedPlayers = false;

    @Setting(value = "hide-player-count", comment = "config.serverlist.hideall")
    private boolean hidePlayerCount = false;

    @NoMergeIfPresent
    @Default("&bWelcome to the server!\n&cCome join us!")
    @Setting(value = "server-list-messages", comment = "config.serverlist.messages")
    public List<NucleusTextTemplateImpl> messages;

    @NoMergeIfPresent
    @Setting(value = "whitelist-server-list-messages", comment = "config.serverlist.whitelistmessages")
    public List<NucleusTextTemplateImpl> whitelist = Lists.newArrayList();

    public boolean isModifyServerList() {
        return modifyServerList;
    }

    public boolean isHideVanishedPlayers() {
        return hideVanishedPlayers;
    }

    public boolean isHidePlayerCount() {
        return hidePlayerCount;
    }

    public List<NucleusTextTemplateImpl> getMessages() {
        return messages;
    }

    public List<NucleusTextTemplateImpl> getWhitelist() {
        return whitelist;
    }

    public boolean enableListener() {
        return modifyServerList || hideVanishedPlayers || hidePlayerCount;
    }
}
