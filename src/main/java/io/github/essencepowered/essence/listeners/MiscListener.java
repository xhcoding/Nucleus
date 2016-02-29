/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.listeners;

import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.internal.ListenerBase;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.interfaces.InternalQuickStartUser;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.io.IOException;

@Modules(PluginModule.MISC)
public class MiscListener extends ListenerBase {

    // Do it first, so other plugins can have a say.
    @Listener(order = Order.FIRST)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        try {
            Player pl = event.getTargetEntity();
            InternalQuickStartUser uc = plugin.getUserLoader().getUser(pl);

            // Let's just reset these...
            uc.setInvulnerable(uc.isInvulnerableSafe());
            uc.setFlying(uc.isFlyingSafe());

            // If in the air, flying!
            if (uc.isFlyingSafe() && pl.getLocation().add(0, -1, 0).getBlockType().getId().equals(BlockTypes.AIR.getId())) {
                pl.offer(Keys.IS_FLYING, true);
            }
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }
}
