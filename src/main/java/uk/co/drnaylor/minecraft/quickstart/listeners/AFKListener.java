/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.listeners;

import com.google.inject.Inject;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.NameUtil;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.PluginModule;
import uk.co.drnaylor.minecraft.quickstart.commands.afk.AFKCommand;
import uk.co.drnaylor.minecraft.quickstart.internal.CommandPermissionHandler;
import uk.co.drnaylor.minecraft.quickstart.internal.ListenerBase;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionRegistry;
import uk.co.drnaylor.minecraft.quickstart.internal.annotations.Modules;

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
        // Did the player run /afk? Then don't do anything, we'll toggle it anyway.
        if (!Arrays.asList(AFKCommand.getAfkAliases()).contains(event.getCommand().toLowerCase())) {
            updateAFK(player);
        }
    }

    private void updateAFK(final Player player) {
        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
            if (plugin.getAfkHandler().updateUserActivity(player.getUniqueId()) && getPermissionUtil().testSuffix(player, "exempt")) {
                MessageChannel.TO_ALL.send(Text.of(TextColors.GRAY, "* ", NameUtil.getName(player), TextColors.GRAY, " " + Util.getMessageWithFormat("afk.fromafk")));
            }
        });
    }
}
