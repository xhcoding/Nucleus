/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.commandlogger.listeners;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.commandlogger.config.CommandLoggerConfig;
import io.github.nucleuspowered.nucleus.modules.commandlogger.config.CommandLoggerConfigAdapter;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;

public class CommandLoggingListener extends ListenerBase {

    @Inject private CommandLoggerConfigAdapter clc;

    @Listener(order = Order.LAST)
    public void onCommand(SendCommandEvent event, @First CommandSource source) {
        // Check source.
        CommandLoggerConfig c = clc.getNodeOrDefault();
        if (source instanceof Player && !c.getLoggerTarget().isLogPlayer()) {
            return;
        } else if (source instanceof CommandBlockSource && !c.getLoggerTarget().isLogCommandBlock()) {
            return;
        } else if (source instanceof ConsoleSource && !c.getLoggerTarget().isLogConsole()) {
            return;
        } else if (!c.getLoggerTarget().isLogOther()) {
            return;
        }

        String command = event.getCommand().toLowerCase();

        // If whitelist, and we have the command, or if not blacklist, and we do not have the command.
        if (c.isWhitelist() == c.getCommandsToFilter().stream().anyMatch(command::equalsIgnoreCase)) {
            plugin.getLogger().info(Util.getMessageWithFormat("commandlog.message", source.getName(), event.getCommand(), event.getArguments()));
        }
    }
}
