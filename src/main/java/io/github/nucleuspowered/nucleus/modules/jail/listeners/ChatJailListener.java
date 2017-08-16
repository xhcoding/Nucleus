/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.jail.JailModule;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfig;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;

import javax.inject.Inject;

public class ChatJailListener extends ListenerBase implements ListenerBase.Conditional {

    private final JailHandler handler;

    @Inject
    public ChatJailListener(JailHandler handler) {
        this.handler = handler;
    }

    @Listener(order = Order.FIRST)
    public void onChat(MessageChannelEvent.Chat event, @Root Player player) {
        if (handler.checkJail(player, false)) {
            player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("jail.muteonchat"));
            event.setCancelled(true);
        }
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(JailModule.ID, JailConfigAdapter.class, JailConfig::isMuteOnJail).orElse(false);
    }

}
