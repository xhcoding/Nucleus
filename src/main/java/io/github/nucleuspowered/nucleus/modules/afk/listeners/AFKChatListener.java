/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.listeners;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.message.MessageChannelEvent;

public class AFKChatListener extends AbstractAFKListener implements ListenerBase.Conditional {

    @Listener
    public void onPlayerChat(final MessageChannelEvent.Chat event) {
        Util.onPlayerSimulatedOrPlayer(event, (e, p) -> update(p));
    }

    @Override
    public boolean shouldEnable() {
        return getTriggerConfigEntry(AFKConfig.Triggers::isOnChat);
    }
}
