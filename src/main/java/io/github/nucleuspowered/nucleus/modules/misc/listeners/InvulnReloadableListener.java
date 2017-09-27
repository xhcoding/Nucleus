/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.misc.MiscModule;
import io.github.nucleuspowered.nucleus.modules.misc.commands.GodCommand;
import io.github.nucleuspowered.nucleus.modules.misc.config.MiscConfig;
import io.github.nucleuspowered.nucleus.modules.misc.config.MiscConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.misc.datamodules.InvulnerabilityUserDataModule;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class InvulnReloadableListener extends ListenerBase implements ListenerBase.Conditional {

    private String basePerm = getPermisisonHandlerFor(GodCommand.class).getBase();

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join joinEvent, @Getter("getTargetEntity") Player player) {
        if (!player.hasPermission(basePerm)) {
            plugin.getUserDataManager().get(player).ifPresent(x -> x.get(InvulnerabilityUserDataModule.class).setInvulnerable(false));
        }
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(MiscModule.ID, MiscConfigAdapter.class, MiscConfig::isGodPermissionOnLogin).orElse(false);
    }

}
