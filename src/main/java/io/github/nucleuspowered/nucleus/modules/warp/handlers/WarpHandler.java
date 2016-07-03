/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.handlers;

import com.flowpowered.math.vector.Vector3d;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.data.WarpLocation;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import io.github.nucleuspowered.nucleus.dataservices.GeneralDataStore;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.Set;

public class WarpHandler implements NucleusWarpService {

    @Inject private GeneralDataStore store;

    @Override
    public Optional<WarpLocation> getWarp(String warpName) {
        return store.getWarpLocation(warpName);
    }

    @Override
    public boolean removeWarp(String warpName) {
        return store.removeWarp(warpName);
    }

    @Override
    public boolean setWarp(String warpName, Location<World> location, Vector3d rotation) {
        return store.addWarp(warpName, location, rotation);
    }

    @Override
    public Set<String> getWarpNames() {
        return store.getWarps().keySet();
    }

    @Override
    public boolean warpExists(String name) {
        return getWarpNames().stream().filter(x -> x.equalsIgnoreCase(name)).findFirst().isPresent();
    }
}
