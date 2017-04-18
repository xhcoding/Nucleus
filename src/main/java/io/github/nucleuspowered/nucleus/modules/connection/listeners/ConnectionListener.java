/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.connection.listeners;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.connection.ConnectionModule;
import io.github.nucleuspowered.nucleus.modules.connection.config.ConnectionConfig;
import io.github.nucleuspowered.nucleus.modules.connection.config.ConnectionConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.util.Tristate;

import java.util.Map;

public class ConnectionListener extends ListenerBase.Reloadable {

    private final String joinFullServer = PermissionRegistry.PERMISSIONS_PREFIX + "connection.joinfullserver";

    private ConnectionConfig connectionConfig;

    /**
     * At the time the player joins if the server is full, check if they are permitted to join a full server.
     *
     * @param event The event.
     */
    @Listener
    @IsCancelled(Tristate.UNDEFINED)
    public void onPlayerJoin(ClientConnectionEvent.Login event, @Getter("getTargetUser") User user) {
        if (Sponge.getServer().hasWhitelist()) {
            if (event.isCancelled()) {
                connectionConfig.getWhitelistMessage().ifPresent(x -> {
                    event.setMessage(x);
                    event.setMessageCancelled(false);
                });
            }

            // Do not continue, whitelist should always apply.
            return;
        }

        if (!(Sponge.getServer().getOnlinePlayers().size() >= Sponge.getServer().getMaxPlayers())) {
            return;
        }

        if (user.hasPermission(joinFullServer)) {
            if (connectionConfig.getReservedSlots() != -1
                    && Sponge.getServer().getOnlinePlayers().size() - Sponge.getServer().getMaxPlayers() >= connectionConfig.getReservedSlots()) {
                return;
            }

            event.setCancelled(false);
        }
    }

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> mp = Maps.newHashMap();
        mp.put(joinFullServer, PermissionInformation.getWithTranslation("permission.connection.joinfullserver", SuggestedLevel.MOD));
        return mp;
    }

    @Override public void onReload() throws Exception {
        connectionConfig = plugin.getConfigAdapter(ConnectionModule.ID, ConnectionConfigAdapter.class).get().getNodeOrDefault();
    }
}
