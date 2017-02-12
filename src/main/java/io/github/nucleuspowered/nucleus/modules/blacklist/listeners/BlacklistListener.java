/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.blacklist.listeners;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.configurate.datatypes.item.BlacklistNode;
import io.github.nucleuspowered.nucleus.dataservices.ItemDataService;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.blacklist.BlacklistModule;
import io.github.nucleuspowered.nucleus.modules.blacklist.config.BlacklistConfig;
import io.github.nucleuspowered.nucleus.modules.blacklist.config.BlacklistConfigAdapter;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import uk.co.drnaylor.quickstart.exceptions.IncorrectAdapterTypeException;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Common listener methods for the listeners.
 */
public abstract class BlacklistListener extends ListenerBase.Reloadable {

    @Inject private ItemDataService itemDataService;
    @Inject BlacklistConfigAdapter bca;
    private boolean isRegisteredInIDS = false;
    private Set<String> ids = null;

    private final String bypass = PermissionRegistry.PERMISSIONS_PREFIX + "blacklist.bypass";

    private final Map<UUID, Instant> messageCache = Maps.newHashMap();

    void sendMessage(Player target, String descRoot, String item, boolean single) {

        UUID u = target.getUniqueId();
        if (!messageCache.containsKey(u) || messageCache.get(u).isAfter(Instant.now())) {
            // Alert the user, but only once a second.
            if (single) {
                target.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat(descRoot + ".single", item));
            } else {
                target.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat(descRoot + ".multiple", item));
            }

            messageCache.put(u, Instant.now().plus(1, ChronoUnit.SECONDS));

            // Cleanup stale entries.
            messageCache.entrySet().removeIf(x -> x.getValue().isAfter(Instant.now()));
        }
    }

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> mp = Maps.newHashMap();
        mp.put(bypass, PermissionInformation.getWithTranslation("permission.blacklist.bypass", SuggestedLevel.ADMIN));
        return mp;
    }

    final boolean hasBypass(Player player, String permissionToCheck) {
        return (player.hasPermission(bypass) || player.hasPermission(permissionToCheck));
    }

    Set<String> getIds(Predicate<Map.Entry<String, BlacklistNode>> predicate) {
        if (ids == null) {
            ids = itemDataService.getAllBlacklistedItems().entrySet()
                .stream().filter(predicate)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        }

        return ids;
    }

    boolean checkBlock(@Nullable BlockSnapshot bs, Set<String> ids) {
        return bs != null && (ids.contains(bs.getState().getId()) || ids.contains(bs.getExtendedState().getId()) || ids.contains(bs.getState().getType().getId()));
    }

    @Override public void onReload() throws Exception {
        if (!isRegisteredInIDS) {
            isRegisteredInIDS = true;
            itemDataService.addOnItemUpdate(() -> ids = null);
            ids = null;
        }
    }

    public abstract static class Condition implements Predicate<Nucleus> {

        public abstract boolean configPredicate(BlacklistConfig config);

        @Override public boolean test(Nucleus nucleus) {
            if (Sponge.getGame().getState().ordinal() < GameState.SERVER_STARTING.ordinal()) {
                return false;
            }

            try {
                return configPredicate(nucleus.getModuleContainer().getConfigAdapterForModule(BlacklistModule.ID, BlacklistConfigAdapter.class)
                    .getNodeOrDefault());
            } catch (NoModuleException | IncorrectAdapterTypeException e) {
                if (nucleus.isDebugMode()) {
                    e.printStackTrace();
                }

                return false;
            }
        }
    }
}
