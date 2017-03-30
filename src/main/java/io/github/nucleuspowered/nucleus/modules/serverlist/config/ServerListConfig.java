/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.serverlist.config;

import com.google.common.collect.Lists;
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
    @Setting(value = "server-list-messages", comment = "config.serverlist.messages")
    public List<NucleusTextTemplateImpl> messages = Lists.newArrayList();

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

    public boolean enableListener() {
        return modifyServerList || hideVanishedPlayers || hidePlayerCount;
    }
}
