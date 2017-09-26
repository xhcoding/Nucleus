/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.listeners;

import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.filter.cause.Root;

public class AFKInteractListener extends AbstractAFKListener implements ListenerBase.Conditional {

    @Listener(order = Order.LAST)
    public void onPlayerInteract(final InteractEvent event, @Root Player player) {
        update(player);
    }

    @Override
    public boolean shouldEnable() {
        return getTriggerConfigEntry(AFKConfig.Triggers::isOnInteract);
    }
}
