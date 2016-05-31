/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.fly.listeners;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DismountEntityEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.World;

public class FlyListener extends ListenerBase {

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
            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }
        }
    }

    // Only fire if there is no cancellation at the end.
    @Listener(order = Order.LAST)
    public void onPlayerTransferWorld(DisplaceEntityEvent event,
                                      @Getter("getTargetEntity") Entity target,
                                      @Getter("getFromTransform") Transform<World> twfrom,
                                      @Getter("getToTransform") Transform<World> twto) {

        if (!(target instanceof Player)) {
            return;
        }

        InternalNucleusUser uc;
        try {
            uc = plugin.getUserLoader().getUser((Player)target);
            if (!uc.isFlying()) {
                return;
            }
        } catch (Exception e) {
            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }

            return;
        }

        // If we have a player, and this happens...
        boolean isFlying = target.get(Keys.IS_FLYING).orElse(false);

        // If we're moving world...
        if (!twfrom.getExtent().getUniqueId().equals(twto.getExtent().getUniqueId())) {
            // Next tick, they can fly...
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                target.offer(Keys.CAN_FLY, true);
                if (isFlying) {
                    target.offer(Keys.IS_FLYING, true);
                }
            }).submit(plugin);
        }
    }

    @Listener
    public void onPlayerDismount(DismountEntityEvent event, @Root Player player) {
        // If I'm right, this will work around Pixelmon when dismounting pokemon.
        try {
            InternalNucleusUser uc = plugin.getUserLoader().getUser(player);
            player.offer(Keys.CAN_FLY, uc.isFlyingSafe());
        } catch (Exception e) {
            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }
        }
    }
}
