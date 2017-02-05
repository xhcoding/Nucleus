/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.listeners;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.events.NucleusFirstJoinEvent;
import io.github.nucleuspowered.nucleus.dataservices.KitService;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class KitListener extends ListenerBase {

    @Inject private UserDataManager loader;
    @Inject private CoreConfigAdapter coreConfigAdapter;
    @Inject private KitHandler handler;
    @Inject private KitService gds;
    @Inject private KitConfigAdapter kitConfigAdapter;

    @Listener
    public void onPlayerFirstJoin(NucleusFirstJoinEvent event, @Getter("getTargetEntity") Player player) {
        loader.get(player).ifPresent(p -> {
            List<ItemStackSnapshot> l = gds.getFirstKit();
            if (l != null && !l.isEmpty()) {
                Util.addToStandardInventory(player, gds.getFirstKit(), false, kitConfigAdapter.getNodeOrDefault().isProcessTokens());
            }
        });
    }

    @Listener
    @Exclude({InteractInventoryEvent.Open.class})
    public void onPlayerInteractInventory(final InteractInventoryEvent event, @Root final Player player, @Getter("getTargetInventory") final Container inventory) {
        handler.getCurrentlyOpenInventoryKit(inventory).ifPresent(x -> {
            try {
                x.getFirst().kit.updateKitInventory(x.getSecond());

                if (event instanceof InteractInventoryEvent.Close) {
                    gds.save();
                    player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.edit.success", x.getFirst().name));
                    handler.removeKitInventoryFromListener(inventory);
                }
            } catch (Exception e) {
                if (coreConfigAdapter.getNodeOrDefault().isDebugmode()) {
                    e.printStackTrace();
                }

                player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.edit.error", x.getFirst().name));
            }
        });

        handler.getFirstJoinKitInventory().ifPresent(x -> {
            if (x.getFirst().equals(inventory)) {
                try {
                    List<Slot> slots = Lists.newArrayList(x.getSecond().slots());
                    gds.setFirstKit(
                        slots.stream().filter(y -> y.peek().isPresent()).map(z -> z.peek().get().createSnapshot()).collect(Collectors.toList()));

                    if (event instanceof InteractInventoryEvent.Close) {
                        gds.save();
                        player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.firstkit.edit.success"));
                        handler.setFirstJoinKitInventory(null);
                    }
                } catch (Exception e) {
                    if (coreConfigAdapter.getNodeOrDefault().isDebugmode()) {
                        e.printStackTrace();
                    }

                    player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.firstkit.edit.error"));
                }
            }
        });
    }
}
