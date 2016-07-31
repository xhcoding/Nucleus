/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.listeners;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.dataservices.GeneralService;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.loaders.WorldDataManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.spawn.config.SpawnConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Map;
import java.util.Optional;

public class SpawnListener extends ListenerBase {

    @Inject private GeneralService store;
    @Inject private UserDataManager loader;
    @Inject private WorldDataManager wcl;
    @Inject private CoreConfigAdapter cca;
    @Inject private SpawnConfigAdapter sca;

    private final String spawnExempt = PermissionRegistry.PERMISSIONS_PREFIX + "spawn.exempt.login";

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> mpi = Maps.newHashMap();
        mpi.put(spawnExempt, new PermissionInformation(Util.getMessageWithFormat("permission.spawn.exempt.login"), SuggestedLevel.ADMIN));
        return mpi;
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join joinEvent) {
        Player pl = joinEvent.getTargetEntity();

        try {
            if (loader.getUser(pl).get().isFirstPlay()) {
                // first spawn.
                Optional<Transform<World>> ofs = store.getFirstSpawn();

                // Bit of an odd line, but what what is going on here is checking for first spawn, and if it exists, then
                // setting the location the player safely. If this cannot be done in either case, send them to world spawn.
                if (!ofs.isPresent() || !pl.setLocationAndRotationSafely(ofs.get().getLocation(), ofs.get().getRotation())) {
                    WorldProperties w = Sponge.getServer().getDefaultWorld().get();
                    pl.setLocation(new Location<>(Sponge.getServer().getWorld(w.getUniqueId()).get(), w.getSpawnPosition().toDouble()));

                    // We don't want to boot them elsewhere.
                    return;
                }
            }
        } catch (Exception e) {
            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }
        }

        // Throw them to the default world spawn if the config suggests so.
        if (sca.getNodeOrDefault().isSpawnOnLogin() && !pl.hasPermission(spawnExempt)) {
            WorldProperties w = Sponge.getServer().getDefaultWorld().get();
            Location<World> lw = new Location<>(Sponge.getServer().getWorld(w.getUniqueId()).get(), w.getSpawnPosition().toDouble());
            try {
                Optional<Vector3d> ov = wcl.getWorld(w.getUniqueId()).get().getSpawnRotation();
                if (ov.isPresent()) {
                    pl.setLocationAndRotation(lw, ov.get());
                }
            } catch (Exception e) {
                //
            }

            pl.setLocation(lw);
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
            Optional<Vector3d> ov = wcl.getWorld(event.getToTransform().getExtent()).get().getSpawnRotation();
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
