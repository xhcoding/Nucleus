/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.blacklist.listeners;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.blacklist.config.BlacklistConfig;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@ConditionalListener(EnvironmentListener.Condition.class)
public class EnvironmentListener extends BlacklistListener {

    private final String environmentRoot = "blacklist.environment";
    private final String environment = PermissionRegistry.PERMISSIONS_PREFIX + "blacklist.bypass.environment";

    @Listener
    public void onPlayerChangeBlock(ChangeBlockEvent event, @Root Player player) {
        if (hasBypass(player, environment)) {
            return;
        }

        Set<String> ids = getIds(x -> x.getValue().isEnvironment());
        List<Transaction<BlockSnapshot>> list = event.getTransactions().stream().filter(Transaction::isValid)
            .filter(x -> checkTransactions(x.getOriginal(), x.getFinal(), ids))
            .collect(Collectors.toList());

        if (!list.isEmpty()) {
            list.forEach(x -> x.setValid(false));
            if (checkBlock(list.get(0).getOriginal(), ids)) {
                sendMessage(player, environmentRoot, Util.getTranslatableIfPresentOnCatalogType(list.get(0).getOriginal().getExtendedState()), list.size() == 1);
            } else {
                sendMessage(player, environmentRoot, Util.getTranslatableIfPresentOnCatalogType(list.get(0).getFinal().getExtendedState()), list.size() == 1);
            }
        }
    }

    private boolean checkTransactions(@Nullable BlockSnapshot original, @Nullable BlockSnapshot fin, Set<String> ids) {
        return checkBlock(original, ids) || checkBlock(fin, ids);
    }

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> mp = Maps.newHashMap();
        mp.put(environment, PermissionInformation.getWithTranslation("permission.blacklist.bypassenvironment", SuggestedLevel.ADMIN));
        return mp;
    }

    public static class Condition extends BlacklistListener.Condition {

        @Override public boolean configPredicate(BlacklistConfig config) {
            return config.getEnvironment();
        }
    }
}
