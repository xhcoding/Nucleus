/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.listeners;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.loaders.WorldDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularGeneralService;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.CoreUserDataModule;
import io.github.nucleuspowered.nucleus.modules.spawn.config.GlobalSpawnConfig;
import io.github.nucleuspowered.nucleus.modules.spawn.config.SpawnConfig;
import io.github.nucleuspowered.nucleus.modules.spawn.config.SpawnConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.spawn.datamodules.SpawnGeneralDataModule;
import io.github.nucleuspowered.nucleus.modules.spawn.datamodules.SpawnWorldDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

public class SpawnListener extends ListenerBase implements Reloadable {

    private final ModularGeneralService store;
    private final UserDataManager loader;
    private final WorldDataManager wcl;
    private final SpawnConfigAdapter sca;
    private SpawnConfig spawnConfig;

    private final String spawnExempt = PermissionRegistry.PERMISSIONS_PREFIX + "spawn.exempt.login";

    @Inject
    public SpawnListener(ModularGeneralService store, UserDataManager loader,
            WorldDataManager wcl, SpawnConfigAdapter sca) {
        this.store = store;
        this.loader = loader;
        this.wcl = wcl;
        this.sca = sca;
    }

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> mpi = Maps.newHashMap();
        mpi.put(spawnExempt, PermissionInformation.getWithTranslation("permission.spawn.exempt.login", SuggestedLevel.ADMIN));
        return mpi;
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Login loginEvent) {
        UUID pl = loginEvent.getProfile().getUniqueId();
        boolean first = loader.getUnchecked(pl).get(CoreUserDataModule.class).isStartedFirstJoin();

        try {
            if (first) {
                // first spawn.
                Optional<Transform<World>> ofs = store.get(SpawnGeneralDataModule.class).getFirstSpawn();

                // Bit of an odd line, but what what is going on here is checking for first spawn, and if it exists, then
                // setting the location the player safely. If this cannot be done in either case, send them to world spawn.
                if (ofs.isPresent()) {
                    NucleusTeleportHandler.TeleportMode mode = spawnConfig.isSafeTeleport() ? NucleusTeleportHandler.TeleportMode.SAFE_TELEPORT : NucleusTeleportHandler.TeleportMode.WALL_CHECK;
                    Optional<Location<World>> location = plugin.getTeleportHandler().getSafeLocation(null, ofs.get().getLocation(), mode);

                    if (location.isPresent()) {
                        loginEvent.setToTransform(new Transform<>(location.get().getExtent(), location.get().getPosition().add(0.5, 0, 0.5), ofs.get().getRotation()));
                        return;
                    }

                    WorldProperties w = Sponge.getServer().getDefaultWorld().get();
                    loginEvent.setToTransform(
                            new Transform<>(new Location<>(Sponge.getServer().getWorld(w.getUniqueId()).get(), w.getSpawnPosition().add(0.5, 0, 0.5))));

                    // We don't want to boot them elsewhere.
                    return;
                }
            }
        } catch (Exception e) {
            if (Nucleus.getNucleus().isDebugMode()) {
                e.printStackTrace();
            }
        }

        // Throw them to the default world spawn if the config suggests so.
        User user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).getOrCreate(loginEvent.getProfile());
        if (spawnConfig.isSpawnOnLogin() && !user.hasPermission(spawnExempt)) {

            GlobalSpawnConfig sc = spawnConfig.getGlobalSpawn();
            World world = loginEvent.getFromTransform().getExtent();
            if (sc.isOnLogin() && sc.getWorld().isPresent()) {
                world = Sponge.getServer().getWorld(sc.getWorld().get().getUniqueId()).orElse(world);
            }

            Location<World> lw = world.getSpawnLocation().add(0.5, 0, 0.5);
            Optional<Location<World>> safe = plugin.getTeleportHandler().getSafeLocation(null, lw,
                    spawnConfig.isSafeTeleport() ? NucleusTeleportHandler.TeleportMode.SAFE_TELEPORT_ASCENDING : NucleusTeleportHandler.TeleportMode.NO_CHECK);
            if (safe.isPresent()) {
                try {
                    Optional<Vector3d> ov = wcl.getWorld(world.getUniqueId()).get().quickGet(SpawnWorldDataModule.class, SpawnWorldDataModule::getSpawnRotation);
                    if (ov.isPresent()) {
                        loginEvent.setToTransform(new Transform<>(safe.get().getExtent(),
                                safe.get().getPosition().add(0.5, 0, 0.5),
                                ov.get()));
                        return;
                    }
                } catch (Exception e) {
                    //
                }

                loginEvent.setToTransform(new Transform<>(safe.get().add(0.5, 0, 0.5)));
            }
        }
    }

    @Listener(order = Order.EARLY)
    public void onPlayerWorldTransfer(MoveEntityEvent.Teleport event) {
        if (event.getTargetEntity() instanceof Player && !event.getFromTransform().getExtent().equals(event.getToTransform().getExtent())) {
            // Are we heading TO a spawn?
            Transform<World> to = event.getToTransform();
            if (to.getLocation().getBlockPosition().equals(to.getExtent().getSpawnLocation().getBlockPosition())) {
                wcl.getWorld(to.getExtent()).ifPresent(x -> x.quickGet(SpawnWorldDataModule.class, SpawnWorldDataModule::getSpawnRotation)
                    .ifPresent(y -> event.setToTransform(to.setRotation(y))));
            }
        }
    }

    @Listener(order = Order.EARLY)
    public void onRespawn(RespawnPlayerEvent event) {
        if (event.isBedSpawn()) {
            // Nope, we don't care.
            return;
        }

        GlobalSpawnConfig sc = spawnConfig.getGlobalSpawn();
        World world = event.getToTransform().getExtent();

        // Get the world.
        if (sc.isOnRespawn()) {
            Optional<WorldProperties> oworld = sc.getWorld();
            if (oworld.isPresent()) {
                world = Sponge.getServer().getWorld(oworld.get().getUniqueId()).orElse(world);
            }
        }

        Location<World> spawn = world.getSpawnLocation().add(0.5, 0, 0.5);
        Transform<World> to = new Transform<>(spawn);

        // Compare current transform to spawn - set rotation.
        wcl.getWorld(world).ifPresent(x -> x.quickGet(SpawnWorldDataModule.class, SpawnWorldDataModule::getSpawnRotation)
            .ifPresent(y -> event.setToTransform(to.setRotation(y))));
    }

    @Override public void onReload() throws Exception {
        spawnConfig = sca.getNodeOrDefault();
    }
}
