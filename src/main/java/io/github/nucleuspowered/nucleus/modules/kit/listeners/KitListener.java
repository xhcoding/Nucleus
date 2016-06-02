/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.listeners;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.GeneralDataStore;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.modules.admin.commands.ExperienceCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KitListener extends ListenerBase {

    @Inject private GeneralDataStore gds;
    @Inject private UserConfigLoader ucl;

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        if (Util.isFirstPlay(player)) {
            List<ItemStackSnapshot> l = gds.getFirstKit();
            if (l != null && !l.isEmpty()) {
                l.forEach(x -> player.getInventory().offer(x.createStack()));
            }
        }

        // Temporary
        try {
            InternalNucleusUser inu = ucl.getUser(player);
            Map<String, Instant> msi = inu.getKitLastUsedTime();

            // Get all the kits.
            Set<String> keys = msi.entrySet().stream().map(k -> k.getKey().toLowerCase()).distinct().collect(Collectors.toSet());
            if (keys.size() == msi.size()) {
                // Kits redeemed is correct. No more processing.
                return;
            }

            MessageChannel mc = MessageChannel.permission("nucleus.staffchat.base");
            mc.send(Text.of(TextColors.RED, "The player " + event.getTargetEntity().getName() + " had multiple kit entries. Repairing..."));
            keys.forEach(k -> {
                long i = msi.entrySet().stream().filter(x -> x.getKey().equalsIgnoreCase(k)).count();
                if (i > 1) {
                    mc.send(Text.of(TextColors.RED, "Player " + event.getTargetEntity().getName() + " had " + i + " entries for " + k));
                }
            });

            // For each kit, get the latest time, then replace it in the map.
            Map<String, Instant> result = Maps.newHashMap();
            keys.forEach(k -> msi.entrySet().stream().filter(x -> x.getKey().equalsIgnoreCase(k)).max((x, y) -> x.getValue().compareTo(y.getValue())).ifPresent(x -> result.put(k, x.getValue())));
            inu.setKitLastUsedTime(result);
        } catch (Exception e) {
            //
        }
    }
}
