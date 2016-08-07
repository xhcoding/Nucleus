/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.listeners;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.dataservices.UserService;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CoreListener extends ListenerBase {

    @Inject private UserDataManager loader;

    /* (non-Javadoc)
     * We do this first to try to get the first play status as quick as possible.
     */
    @Listener(order = Order.FIRST)
    public void onPlayerJoinFirst(final ClientConnectionEvent.Join event) {
        try {
            Player player = event.getTargetEntity();
            UserService qsu = loader.get(player).get();
            qsu.setLastLogin(Instant.now());
            qsu.setFirstPlay(Util.isFirstPlay(player));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoinLast(final ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        UserService qsu = loader.get(player).get();

        // If we have a location to send them to in the config, send them there now!
        Optional<Location<World>> olw = qsu.getLocationOnLogin();
        if (olw.isPresent()) {
            event.getTargetEntity().setLocation(olw.get());
            qsu.removeLocationOnLogin();
        }
    }

    @Listener
    public void onPlayerQuit(final ClientConnectionEvent.Disconnect event) {
        Sponge.getScheduler().createAsyncExecutor(plugin).schedule(() -> {
            try {
                this.plugin.getUserDataManager().get(event.getTargetEntity()).ifPresent(UserService::setOnLogout);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 200, TimeUnit.MILLISECONDS);
    }

    @Listener
    public void onGameReload(final GameReloadEvent event) {
        plugin.reload();
        CommandSource requester = event.getCause().first(CommandSource.class).orElse(Sponge.getServer().getConsole());
        requester.sendMessage(Text.of(TextColors.YELLOW, "[Nucleus] ", Util.getTextMessageWithFormat("command.reload.one")));
        requester.sendMessage(Text.of(TextColors.YELLOW, "[Nucleus] ", Util.getTextMessageWithFormat("command.reload.two")));
    }
}
