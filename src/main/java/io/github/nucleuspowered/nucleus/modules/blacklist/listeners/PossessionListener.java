/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.blacklist.listeners;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.blacklist.config.BlacklistConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.scheduler.Task;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@ConditionalListener(PossessionListener.Condition.class)
public class PossessionListener extends BlacklistListener {

    private final String possess = PermissionRegistry.PERMISSIONS_PREFIX + "blacklist.bypass.possesion";
    private final String confiscateRoot = "blacklist.confiscate";

    @Nullable private ItemStackSnapshot replacementItem = null;

    @Listener
    public void onOpenInventory(InteractInventoryEvent.Open event, @Root Player player) {
        Inventory inventory;
        if (isPlayerInventory(event.getTargetInventory(), player)) {
            inventory = event.getTargetInventory();
        } else {
            inventory = player.getInventory();
        }

        testInventory(inventory, player);
    }

    @Listener
    public void onCloseInventory(InteractInventoryEvent.Close event, @Root Player player) {
        testInventory(player.getInventory(), player);
    }

    private boolean testInventory(Inventory inventory, @Root Player player) {
        if (hasBypass(player, possess)) {
            return false;
        }

        // Scan inventory.
        final Set<String> ids = getIds(x -> x.getValue().isInventory());

        int replaced = 0;
        String item = null;

        for (Inventory x : inventory.slots()) {
            Optional<ItemStack> itemStackOptional = x.peek();
            if (itemStackOptional.isPresent()) {
                ItemStack y = itemStackOptional.get();
                if (ids.contains(Util.getTypeFromItem(y).getId())) {
                    if (replacementItem == null) {
                        x.clear();
                    } else {
                        x.set(replacementItem.createStack());
                    }

                    replaced++;
                    if (item == null) {
                        item = Util.getTranslatableIfPresentOnCatalogType(y.getItem().getType());
                    }
                }
            }
        }

        if (replaced > 0) {
            sendMessage(player, confiscateRoot, item, true);
            return true;
        }

        return false;
    }

    @Listener
    public void onChangeItem(ChangeInventoryEvent event, @Root Player player) {
        if (hasBypass(player, possess)) {
            return;
        }

        if (isPlayerInventory(event.getTargetInventory(), player)) {
            final Set<String> ids = getIds(x -> x.getValue().isInventory());
            event.getTransactions().forEach(x -> {
                if (ids.contains(Util.getTypeFromItem(x.getFinal()).getId())) {
                    x.setValid(false);
                }
            });

            Task.builder().execute(() -> testInventory(event.getTargetInventory(), player)).submit(plugin);
        }
    }

    @Listener
    @Exclude({ClickInventoryEvent.Close.class, ClickInventoryEvent.Open.class})
    public void onClick(ClickInventoryEvent event, @Root Player player) {
        if (hasBypass(player, possess)) {
            return;
        }

        if (isPlayerInventory(event.getTargetInventory(), player)) {
            final Set<String> ids = getIds(x -> x.getValue().isInventory());
            if (ids.contains(Util.getTypeFromItem(event.getCursorTransaction().getOriginal()).getId())) {
                event.setCancelled(true);
                sendMessage(player, confiscateRoot, Util.getTranslatableIfPresentOnCatalogType(event.getCursorTransaction().getOriginal().getType()), true);
            } else if (ids.contains(Util.getTypeFromItem(event.getCursorTransaction().getFinal()).getId())) {
                event.setCancelled(true);
                sendMessage(player, confiscateRoot, Util.getTranslatableIfPresentOnCatalogType(event.getCursorTransaction().getFinal().getType()), true);
            }

            testInventory(event.getTargetInventory(), player);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isPlayerInventory(Inventory container, Player player) {
        try {
            return container instanceof CarriedInventory && (boolean) ((CarriedInventory) container).getCarrier()
                    .map(x -> x instanceof Player && ((Player) x).getUniqueId().equals(player.getUniqueId())).orElse(false);
        } catch (Exception e) {
            if (Nucleus.getNucleus().isDebugMode()) {
                e.printStackTrace();
            }

            return false;
        }
    }

    @Override public void onReload() throws Exception {
        super.onReload();
        replacementItem = bca.getNodeOrDefault().getItemStackIfShouldUseReplacement().orElse(null);
    }

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> mp = Maps.newHashMap();
        mp.put(possess, PermissionInformation.getWithTranslation("permission.blacklist.bypasspossess", SuggestedLevel.ADMIN));
        return mp;
    }

    public static class Condition extends BlacklistListener.Condition {

        @Override public boolean configPredicate(BlacklistConfig config) {
            return config.getPossession();
        }
    }
}
