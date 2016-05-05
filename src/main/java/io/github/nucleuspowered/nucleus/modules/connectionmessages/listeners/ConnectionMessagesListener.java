/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.connectionmessages.listeners;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.ChatUtil;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.connectionmessages.config.ConnectionMessagesConfig;
import io.github.nucleuspowered.nucleus.modules.connectionmessages.config.ConnectionMessagesConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class ConnectionMessagesListener extends ListenerBase {

    @Inject private ChatUtil chatUtil;
    @Inject private ConnectionMessagesConfigAdapter cma;

    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Join joinEvent) {
        Player pl = joinEvent.getTargetEntity();
        ConnectionMessagesConfig cmc = cma.getNodeOrDefault();

        if (Util.isFirstPlay(pl)) {
            // First time player.
            if (cmc.isShowFirstTimeMessage() && !cmc.getFirstTimeMessage().isEmpty()) {
                Sponge.getServer().getBroadcastChannel().send(plugin, chatUtil.getFromTemplate(cma.getNodeOrDefault().getFirstTimeMessage(), pl, true));
            }
        }

        if (cmc.isModifyLoginMessage()) {
            if (cmc.getLoginMessage().isEmpty()) {
                joinEvent.setMessageCancelled(true);
            } else {
                joinEvent.setMessage(chatUtil.getFromTemplate(cma.getNodeOrDefault().getLoginMessage(), pl, true));
            }
        }
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect leaveEvent) {
        Player pl = leaveEvent.getTargetEntity();
        ConnectionMessagesConfig cmc = cma.getNodeOrDefault();

        if (cmc.isModifyLogoutMessage()) {
            if (cmc.getLogoutMessage().isEmpty()) {
                leaveEvent.setMessageCancelled(true);
            } else {
                leaveEvent.setMessage(chatUtil.getFromTemplate(cma.getNodeOrDefault().getLogoutMessage(), pl, true));
            }
        }
    }
}
