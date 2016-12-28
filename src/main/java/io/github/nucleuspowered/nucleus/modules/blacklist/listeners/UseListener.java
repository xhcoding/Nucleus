/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.blacklist.listeners;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.blacklist.config.BlacklistConfig;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Map;

@ConditionalListener(UseListener.Condition.class)
public class UseListener extends BlacklistListener {

    private final String use = PermissionRegistry.PERMISSIONS_PREFIX + "blacklist.bypass.use";
    private final String useRoot = "blacklist.use";

    @Listener
    public void onPlayerUseItem(UseItemStackEvent.Start event, @Root Player player, @Getter("getItemStackInUse")ItemStackSnapshot itemStackSnapshot) {
        if (hasBypass(player, use)) {
            return;
        }

        if (getIds(x -> x.getValue().isUse()).contains(Util.getTypeFromItem(itemStackSnapshot).getId())) {
            event.setCancelled(true);
            sendMessage(player, useRoot, Util.getTranslatableIfPresentOnCatalogType(itemStackSnapshot.getType()), true);
        }
    }

    @Listener
    public void onPlayerInteractBlock(InteractBlockEvent.Secondary event, @Root Player player, @Getter("getTargetBlock") BlockSnapshot blockSnapshot) {
        if (hasBypass(player, use)) {
            return;
        }

        if (this.checkBlock(blockSnapshot, getIds(x -> x.getValue().isUse()))) {
            event.setCancelled(true);
            sendMessage(player, useRoot, Util.getTranslatableIfPresentOnCatalogType(blockSnapshot.getState().getType()), true);
        }
    }

    @Override public void onReload() throws Exception {}

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> mp = super.getPermissions();
        mp.put(use, new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.blacklist.bypassuse"), SuggestedLevel.ADMIN));
        return mp;
    }

    public static class Condition extends BlacklistListener.Condition {

        @Override public boolean configPredicate(BlacklistConfig config) {
            return config.getUse();
        }
    }
}
