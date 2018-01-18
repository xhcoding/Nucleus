/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish.listener;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.ServiceChangeListener;
import io.github.nucleuspowered.nucleus.modules.vanish.commands.VanishCommand;
import io.github.nucleuspowered.nucleus.modules.vanish.config.VanishConfig;
import io.github.nucleuspowered.nucleus.modules.vanish.config.VanishConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.vanish.datamodules.VanishUserDataModule;
import io.github.nucleuspowered.nucleus.modules.vanish.service.VanishService;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class VanishListener extends ListenerBase implements Reloadable {

    private VanishConfig vanishConfig = new VanishConfig();
    private VanishService service = getServiceUnchecked(VanishService.class);

    private final String permission = getPermissionHandlerFor(VanishCommand.class).getPermissionWithSuffix("persist");
    private final String loginVanishPermission = getPermissionHandlerFor(VanishCommand.class).getPermissionWithSuffix("onlogin");

    @Listener(order = Order.FIRST)
    public void onLogin(ClientConnectionEvent.Login event, @Root Player player) {
        if (!ServiceChangeListener.isOpOnly() && player.hasPermission(this.loginVanishPermission)) {
            service.vanishPlayer(player);
        }
    }

    @Listener
    public void onLogin(ClientConnectionEvent.Join event, @Root Player player) {
        if (service.isVanished(player)) {
            if (!player.hasPermission(this.permission)) {
                // No permission, no vanish.
                service.unvanishPlayer(player);
                return;
            } else if (vanishConfig.isSuppressMessagesOnVanish()) {
                event.setMessageCancelled(true);
            }

            this.service.vanishPlayer(player);
            player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("vanish.login"));
        }
    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event, @Root Player player) {
        if (player.get(Keys.VANISH).orElse(false)) {
            Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(VanishUserDataModule.class).setVanished(true);
            if (vanishConfig.isSuppressMessagesOnVanish()) {
                event.setMessageCancelled(true);
            }
        }
    }

    @Override
    public void onReload() throws Exception {
        this.vanishConfig = getServiceUnchecked(VanishConfigAdapter.class).getNodeOrDefault();
    }
}
