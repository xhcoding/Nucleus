/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.playerinfo.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.playerinfo.PlayerInfoModule;
import io.github.nucleuspowered.nucleus.modules.playerinfo.config.PlayerInfoConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;

public class CommandListener extends ListenerBase implements ListenerBase.Conditional {

    private boolean messageShown = false;

    @Listener
    public void onCommandPreProcess(SendCommandEvent event, @Root ConsoleSource source, @Getter("getCommand") String command) {
        if (command.equalsIgnoreCase("list")) {
            event.setCommand("minecraft:list");
            if (!messageShown) {
                messageShown = true;
                Sponge.getScheduler().createSyncExecutor(plugin).submit(() ->
                    source.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("list.listener.multicraftcompat")));
            }
        }
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(PlayerInfoModule.ID, PlayerInfoConfigAdapter.class, x -> x.getList().isPanelCompatibility())
                .orElse(false);
    }

}
