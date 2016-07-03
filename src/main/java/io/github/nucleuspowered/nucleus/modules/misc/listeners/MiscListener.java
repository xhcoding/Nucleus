/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.listeners;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DamageEntityEvent;

public class MiscListener extends ListenerBase {

    @Inject private UserDataManager ucl;
    @Inject private CoreConfigAdapter cca;

    // For /god
    @Listener
    public void onPlayerStruck(DamageEntityEvent event) {
        if (event.getTargetEntity() instanceof Player) {
            Player pl = (Player)event.getTargetEntity();
            if (isInvulnerable(pl)) {
                event.setBaseDamage(0);
                event.setCancelled(true);
            }
        }
    }

    private boolean isInvulnerable(Player pl) {
        try {
            return ucl.getUser(pl).get().isInvulnerable();
        } catch (Exception e) {
            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }

            return false;
        }
    }
}
