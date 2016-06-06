/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.listeners;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
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

    @Listener(order = Order.FIRST)
    public void onPlayerLogin(final ClientConnectionEvent.Login event) {
        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
            try {
                InternalNucleusUser qsu = this.plugin.getUserLoader().getUser(event.getTargetUser());
                qsu.setLastLogin(Instant.now());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Listener(order = Order.DEFAULT)
    public void onPlayerJoin(final ClientConnectionEvent.Join event) {
        try {
            InternalNucleusUser qsu = this.plugin.getUserLoader().getUser(event.getTargetEntity());

            // If we have a location to send them to in the config, send them there now!
            Optional<Location<World>> olw = qsu.getLocationOnLogin();
            if (olw.isPresent()) {
                event.getTargetEntity().setLocation(olw.get());
                qsu.removeLocationOnLogin();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onPlayerQuit(final ClientConnectionEvent.Disconnect event) {
        Sponge.getScheduler().createAsyncExecutor(plugin).schedule(() -> {
            UserConfigLoader ucl = this.plugin.getUserLoader();
            try {
                InternalNucleusUser qsu = this.plugin.getUserLoader().getUser(event.getTargetEntity());
                qsu.setOnLogout();
                ucl.purgeNotOnline();
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
