/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.listeners;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;

@Modules(PluginModule.COMMAND_LOGGING)
public class CommandLoggingListener extends ListenerBase {

    @Listener(order = Order.LAST)
    public void onCommand(SendCommandEvent event, @First CommandSource source) {
        plugin.getLogger().info(Util.getMessageWithFormat("commandlog", source.getName(), event.getCommand(), event.getArguments()));
    }
}
