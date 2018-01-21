/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.connectionmessages.listeners;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.events.NucleusFirstJoinEvent;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.ServiceChangeListener;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.connectionmessages.config.ConnectionMessagesConfig;
import io.github.nucleuspowered.nucleus.modules.connectionmessages.config.ConnectionMessagesConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.CoreUserDataModule;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConnectionMessagesListener extends ListenerBase implements Reloadable {

    private ConnectionMessagesConfig cmc = new ConnectionMessagesConfig();
    private final String disablePermission = PermissionRegistry.PERMISSIONS_PREFIX + "connectionmessages.disable";

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        return new HashMap<String, PermissionInformation>() {{
            put(disablePermission, new PermissionInformation(
                plugin.getMessageProvider().getMessageWithFormat("permission.connectionmesssages.disable"),
                SuggestedLevel.NONE));
        }};
    }

    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Join joinEvent, @Getter("getTargetEntity") Player pl) {
        if (joinEvent.isMessageCancelled() || (cmc.isDisableWithPermission() && pl.hasPermission(this.disablePermission))) {
            joinEvent.setMessageCancelled(true);
            return;
        }

        try {
            ModularUserService nucleusUser = Nucleus.getNucleus().getUserDataManager().getUnchecked(pl);
            Optional<String> lastKnown = nucleusUser.get(CoreUserDataModule.class).getLastKnownName();
            if (cmc.isDisplayPriorName() &&
                !cmc.getPriorNameMessage().isEmpty() &&
                !lastKnown.orElseGet(pl::getName).equalsIgnoreCase(pl.getName())) {
                    // Name change!
                    joinEvent.getChannel().orElse(MessageChannel.TO_ALL).send(plugin,
                        cmc.getPriorNameMessage().getForCommandSource(pl,
                            ImmutableMap.of("previousname", cs -> Optional.of(Text.of(lastKnown.get()))), Maps.newHashMap()));
            }
        } catch (Exception e) {
            if (Nucleus.getNucleus().isDebugMode()) {
                e.printStackTrace();
            }
        }

        if (cmc.isModifyLoginMessage()) {
            if (cmc.getLoginMessage().isEmpty()) {
                joinEvent.setMessageCancelled(true);
            } else {
                joinEvent.setMessage(cmc.getLoginMessage().getForCommandSource(pl));
            }
        }
    }

    @Listener
    public void onPlayerFirstJoin(NucleusFirstJoinEvent event, @Getter("getTargetEntity") Player pl) {
        if (cmc.isShowFirstTimeMessage() && !cmc.getFirstTimeMessage().isEmpty()) {
            event.getChannel().orElse(MessageChannel.TO_ALL).send(plugin, cmc.getFirstTimeMessage().getForCommandSource(pl));
        }
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect leaveEvent, @Getter("getTargetEntity") Player pl) {
        if (leaveEvent.isMessageCancelled() || (cmc.isDisableWithPermission() && pl.hasPermission(this.disablePermission))) {
            leaveEvent.setMessageCancelled(true);
            return;
        }

        if (cmc.isModifyLogoutMessage()) {
            if (cmc.getLogoutMessage().isEmpty()) {
                leaveEvent.setMessageCancelled(true);
            } else {
                leaveEvent.setMessage(cmc.getLogoutMessage().getForCommandSource(pl));
            }
        }
    }

    @Override
    public void onReload() throws Exception {
        cmc = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(ConnectionMessagesConfigAdapter.class).getNodeOrDefault();
    }
}
