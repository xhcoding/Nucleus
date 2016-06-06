/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.listeners;

import io.github.nucleuspowered.nucleus.config.GeneralDataStore;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import javax.inject.Inject;
import java.util.List;

public class KitListener extends ListenerBase {

    @Inject private GeneralDataStore gds;

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        if (!player.hasPlayedBefore()) {
            List<ItemStackSnapshot> l = gds.getFirstKit();
            if (l != null && !l.isEmpty()) {
                l.forEach(x -> player.getInventory().offer(x.createStack()));
            }
        }
    }
}
