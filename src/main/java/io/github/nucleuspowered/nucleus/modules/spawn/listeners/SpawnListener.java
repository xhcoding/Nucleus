/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.listeners;

import com.flowpowered.math.vector.Vector3d;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.GeneralDataStore;
import io.github.nucleuspowered.nucleus.config.loaders.WorldConfigLoader;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

public class SpawnListener extends ListenerBase {

    @Inject private GeneralDataStore store;
    @Inject private WorldConfigLoader wcl;
    @Inject private CoreConfigAdapter cca;

    // No point in having a filter here - only players are going to appear!
    @Listener
    public void onJoin(ClientConnectionEvent.Join joinEvent) {

        Player pl = joinEvent.getTargetEntity();

        if (Util.isFirstPlay(pl)) {
            // first spawn.
            Optional<Transform<World>> ofs = store.getFirstSpawn();

            // Bit of an odd line, but what what is going on here is checking for first spawn, and if it exists, then
            // setting the location the player safely. If this cannot be done in either case, send them to world spawn.
            if (!ofs.isPresent() || !pl.setLocationAndRotationSafely(ofs.get().getLocation(), ofs.get().getRotation())) {
                WorldProperties w = Sponge.getServer().getDefaultWorld().get();
                pl.setLocation(new Location<>(Sponge.getServer().getWorld(w.getUniqueId()).get(), w.getSpawnPosition().toDouble()));
            }
        }
    }

    @Listener
    public void onRespawn(RespawnPlayerEvent event) {
        if (event.isBedSpawn()) {
            // Nope, we don't care.
            return;
        }

        // If we have a spawn to the world in the standard spawn location, add our rotation.
        try {
            // ...of course, that depends on our rotation.
            Optional<Vector3d> ov = wcl.getWorld(event.getToTransform().getExtent()).getSpawnRotation();
            if (ov.isPresent()) {
                // Compare current transform to spawn.
                Transform<World> to = event.getToTransform();
                Location<World> spawn = event.getToTransform().getExtent().getSpawnLocation();

                // We're spawning in the same place if this is true, so set the rotation.
                if (to.getLocation().getBlockPosition().equals(spawn.getBlockPosition())) {
                    to.setRotation(ov.get());
                    event.setToTransform(to);
                }
            }
        } catch (Exception e) {
            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }
        }
    }
}
