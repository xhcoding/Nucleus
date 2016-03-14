/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.listeners;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.api.data.NucleusUser;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import io.github.nucleuspowered.nucleus.internal.services.datastore.UserConfigLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

import java.io.IOException;

@Modules(PluginModule.ADMIN)
public class AdminListener extends ListenerBase {

    @Inject private UserConfigLoader ucl;

    @Listener
    public void onPlayerMovement(DisplaceEntityEvent.Move event, @First Player player) {
        NucleusUser nu;
        try {
            nu = ucl.getUser(player);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return;
        }

        if (nu.isFrozen()) {
            player.sendMessage(Util.getTextMessageWithFormat("freeze.cancelmove"));
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerInteractBlock(InteractEvent event, @First Player player) {
        NucleusUser nu;
        try {
            nu = ucl.getUser(player);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return;
        }

        if (nu.isFrozen()) {
            player.sendMessage(Util.getTextMessageWithFormat("freeze.cancelinteract"));
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerInteractBlock(InteractBlockEvent event, @First Player player) {
        NucleusUser nu;
        try {
            nu = ucl.getUser(player);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return;
        }

        if (nu.isFrozen()) {
            player.sendMessage(Util.getTextMessageWithFormat("freeze.cancelinteractblock"));
            event.setCancelled(true);
        }
    }
}
