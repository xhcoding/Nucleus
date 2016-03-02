/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.listeners;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.service.EssenceWarmupManagerService;
import io.github.essencepowered.essence.internal.ListenerBase;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class WarmupListener extends ListenerBase {

    private EssenceWarmupManagerService service = Sponge.getGame().getServiceManager().provideUnchecked(EssenceWarmupManagerService.class);

    @Listener(order = Order.LAST)
    public void onPlayerMovement(DisplaceEntityEvent.Move event, @First Player player) {
        // Rotating is OK!
        if (event.getFromTransform().getLocation().equals(event.getToTransform().getLocation())) {
            cancelWarmup(player);
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerCommand(SendCommandEvent event, @First Player player) {
        cancelWarmup(player);
    }

    @Listener(order = Order.LAST)
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        cancelWarmup(event.getTargetEntity());
    }

    private void cancelWarmup(Player player) {
        service.cleanup();
        if (service.removeWarmup(player.getUniqueId()) && player.isOnline()) {
            player.sendMessage(Util.getTextMessageWithFormat("warmup.cancel"));
        }
    }
}
