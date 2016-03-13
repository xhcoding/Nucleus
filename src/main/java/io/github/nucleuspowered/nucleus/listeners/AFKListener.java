/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.listeners;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.NameUtil;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.commands.afk.AFKCommand;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.Arrays;

@Modules(PluginModule.AFK)
public class AFKListener extends ListenerBase {

    @Inject private PermissionRegistry permissionRegistry;

    private CommandPermissionHandler s = null;

    private CommandPermissionHandler getPermissionUtil() {
        if (s == null) {
            s = permissionRegistry.getService(AFKCommand.class).orElseGet(() -> new CommandPermissionHandler(new AFKCommand()));
        }

        return s;
    }

    @Listener(order = Order.FIRST)
    public void onPlayerLogin(final ClientConnectionEvent.Login event) {
        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> plugin.getAfkHandler().login(event.getTargetUser().getUniqueId()));
    }

    @Listener(order = Order.LAST)
    public void onPlayerInteract(final InteractEvent event, @First Player player) {
        updateAFK(player);
    }

    @Listener(order = Order.LAST)
    public void onPlayerMove(final DisplaceEntityEvent event, @First Player player) {
        updateAFK(player);
    }

    @Listener
    public void onPlayerChat(final MessageChannelEvent.Chat event, @First Player player) {
        updateAFK(player);
    }

    @Listener
    public void onPlayerCommand(final SendCommandEvent event, @First Player player) {
        // Did the player run /afk? Then don't do anything, we'll toggle it
        // anyway.
        if (!Arrays.asList(AFKCommand.class.getAnnotation(RegisterCommand.class).value()).contains(event.getCommand().toLowerCase())) {
            updateAFK(player);
        }
    }

    private void updateAFK(final Player player) {
        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
            if (plugin.getAfkHandler().updateUserActivity(player.getUniqueId()) && !getPermissionUtil().testSuffix(player, "exempt.toggle")) {
                MessageChannel.TO_ALL.send(Util.getTextMessageWithFormat("afk.fromafk", NameUtil.getSerialisedName(player)));
            }
        });
    }
}
