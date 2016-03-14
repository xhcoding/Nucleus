/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.listeners;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.api.data.NucleusUser;
import io.github.nucleuspowered.nucleus.config.MainConfig;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

@Modules(PluginModule.ADMIN)
public class AdminListener extends ListenerBase {

    @Inject private UserConfigLoader ucl;
    @Inject private MainConfig main;

    @Listener
    public void onPlayerMovement(DisplaceEntityEvent.Move event, @First Player player) {
        event.setCancelled(checkForFrozen(player, "freeze.cancelmove"));
    }

    @Listener
    public void onPlayerInteractBlock(InteractEvent event, @First Player player) {
        event.setCancelled(checkForFrozen(player, "freeze.cancelinteract"));
    }

    @Listener
    public void onPlayerInteractBlock(InteractBlockEvent event, @First Player player) {
        event.setCancelled(checkForFrozen(player, "freeze.cancelinteractblock"));
    }

    private boolean checkForFrozen(Player player, String message) {
        NucleusUser nu;
        try {
            nu = ucl.getUser(player);
        } catch (Exception e) {
            if (main.getDebugMode()) {
                e.printStackTrace();
            }

            return false;
        }

        if (nu.isFrozen()) {
            player.sendMessage(Util.getTextMessageWithFormat(message));
            return true;
        }

        return false;
    }
}
