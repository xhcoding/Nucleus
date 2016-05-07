/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.listeners;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.config.GeneralDataStore;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

public class SpawnListener extends ListenerBase {

    @Inject private GeneralDataStore store;

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
}
