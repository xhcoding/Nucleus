/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.listeners;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.PluginModule;
import io.github.nucleuspowered.nucleus.commands.powertool.PowertoolCommand;
import io.github.nucleuspowered.nucleus.config.MainConfig;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.Modules;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.internal.services.datastore.UserConfigLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.ItemType;

import java.io.IOException;

@Modules(PluginModule.POWERTOOL)
public class PowertoolListener extends ListenerBase {

    @Inject private UserConfigLoader loader;
    @Inject private MainConfig config;

    @Inject private PermissionRegistry permissionRegistry;

    private CommandPermissionHandler s = null;

    private CommandPermissionHandler getPermissionUtil() {
        if (s == null) {
            s = permissionRegistry.getService(PowertoolCommand.class).orElseGet(() -> new CommandPermissionHandler(new PowertoolCommand()));
        }

        return s;
    }

    @Listener
    public void onUserInteract(final InteractEvent event, @Root Player player) {
        // No item in hand or no permission -> no powertool.
        if (!getPermissionUtil().testBase(player) || !player.getItemInHand().isPresent()) {
            return;
        }

        // Get the item and the user.
        ItemType item = player.getItemInHand().get().getItem();
        InternalNucleusUser user;
        try {
            user = loader.getUser(player);
        } catch (IOException | ObjectMappingException e) {
            if (config.getDebugMode()) {
                e.printStackTrace();
            }

            return;
        }

        // If the powertools are toggled on.
        if (user.isPowertoolToggled()) {
            // Execute all powertools if they exist.
            user.getPowertoolForItem(item).ifPresent(x -> {
                // Cancel the interaction.
                event.setCancelled(true);

                // Run each command.
                x.forEach(s -> Sponge.getCommandManager().process(player, s));
            });
        }
    }
}
