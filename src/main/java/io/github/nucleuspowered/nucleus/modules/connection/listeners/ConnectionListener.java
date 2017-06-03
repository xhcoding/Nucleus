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
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;

import java.util.Map;

import javax.annotation.Nullable;

public class ConnectionListener extends ListenerBase.Reloadable {

    private final String joinFullServer = PermissionRegistry.PERMISSIONS_PREFIX + "connection.joinfullserver";

    private int reservedSlots = 0;
    @Nullable private Text whitelistMessage;

    /**
     * Perform connection events on when a player is currently not permitted to join.
     *
     * @param event The event.
     */
    @Listener(order = Order.FIRST)
    @IsCancelled(Tristate.TRUE)
    public void onPlayerJoinAndCancelled(ClientConnectionEvent.Login event, @Getter("getTargetUser") User user) {
        // Don't affect the banned.
        BanService banService = Sponge.getServiceManager().provideUnchecked(BanService.class);
        if (banService.isBanned(user.getProfile()) || banService.isBanned(event.getConnection().getAddress().getAddress())) {
            return;
        }

        if (Sponge.getServer().hasWhitelist()) {
            if (this.whitelistMessage != null) {
                event.setMessage(this.whitelistMessage);
                event.setMessageCancelled(false);
            }

            // Do not continue, whitelist should always apply.
            return;
        }

        if (user.hasPermission(this.joinFullServer)) {

            // online >= max, so online - max will always >= 0
            if (this.reservedSlots <= -1 ||
                    Sponge.getServer().getOnlinePlayers().size() - Sponge.getServer().getMaxPlayers() < this.reservedSlots) {
                event.setCancelled(false);
            }
        }
    }

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> mp = Maps.newHashMap();
        mp.put(this.joinFullServer, PermissionInformation.getWithTranslation("permission.connection.joinfullserver", SuggestedLevel.MOD));
        return mp;
    }

    @Override public void onReload() throws Exception {
        ConnectionConfig connectionConfig = this.plugin.getConfigAdapter(ConnectionModule.ID, ConnectionConfigAdapter.class).get().getNodeOrDefault();
        this.reservedSlots = connectionConfig.getReservedSlots();
        this.whitelistMessage = connectionConfig.getWhitelistMessage().orElse(null);
    }
}
