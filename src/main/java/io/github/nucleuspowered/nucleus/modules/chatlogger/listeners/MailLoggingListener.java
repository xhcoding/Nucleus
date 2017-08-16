/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chatlogger.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.events.NucleusMailEvent;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.chatlogger.ChatLoggerModule;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.chatlogger.handlers.ChatLoggerHandler;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;

import javax.inject.Inject;

public class MailLoggingListener extends ListenerBase implements ListenerBase.Conditional {

    private final ChatLoggerHandler handler;

    @Inject
    public MailLoggingListener(ChatLoggerHandler handler) {
        this.handler = handler;
    }

    @Listener(order = Order.LAST)
    public void onCommand(NucleusMailEvent event, @First CommandSource source) {
        String message = plugin.getMessageProvider().getMessageWithFormat("chatlog.mail",
            source.getName(), event.getRecipient().getName(), event.getMessage());
        handler.queueEntry(message);
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(ChatLoggerModule.ID, ChatLoggingConfigAdapter.class, x -> x.isEnableLog() && x.isLogMail())
                .orElse(false);
    }

}
