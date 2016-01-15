package uk.co.drnaylor.minecraft.quickstart.api.service;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * Gets a service that allows users to
 */
public interface QuickStartWarpService {

    /**
     *
     *
     * @param warpName
     * @return
     */
    Optional<Location<World>> getWarp(String warpName);

    boolean removeWarp(String warpName);

    boolean setWarp(String warpName, Location<World> location);
}
