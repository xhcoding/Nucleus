/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.listeners;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class MiscListener extends ListenerBase {

    @Inject private UserConfigLoader ucl;
    @Inject private CoreConfigAdapter cca;

    // Do it first, so other plugins can have a say.
    @Listener(order = Order.FIRST)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        try {
            Player pl = event.getTargetEntity();
            InternalNucleusUser uc = plugin.getUserLoader().getUser(pl);

            // Let's just reset these...
            uc.setFlying(uc.isFlyingSafe());

            // If in the air, flying!
            if (uc.isFlyingSafe() && pl.getLocation().add(0, -1, 0).getBlockType().getId().equals(BlockTypes.AIR.getId())) {
                pl.offer(Keys.IS_FLYING, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // For /god
    @Listener
    public void onPlayerStruck(DamageEntityEvent event) {
        if (event.getTargetEntity() instanceof Player) {
            Player pl = (Player)event.getTargetEntity();
            try {
                if (ucl.getUser(pl).isInvulnerable()) {
                    event.setBaseDamage(0);
                    event.setCancelled(true);
                }
            } catch (Exception e) {
                if (cca.getNodeOrDefault().isDebugmode()) {
                    e.printStackTrace();
                }
            }
        }
    }
}
