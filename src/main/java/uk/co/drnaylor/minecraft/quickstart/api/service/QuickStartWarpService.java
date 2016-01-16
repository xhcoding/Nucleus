package uk.co.drnaylor.minecraft.quickstart.api.service;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.Set;

/**
 * Gets a service that allows users to warp about using defined warps.
 */
public interface QuickStartWarpService {

    /**
     * Gets the location for the specified warp.
     *
     * @param warpName The name of the warp to check.
     * @return The {@link Location} of the warp, or {@link Optional#empty()} otherwise.
     */
    Optional<Location<World>> getWarp(String warpName);

    /**
     * Removes a warp.
     *
     * @param warpName The name of the warp.
     * @return <code>true</code> if the warp was there and removed, <code>false</code> if the warp never existed.
     */
    boolean removeWarp(String warpName);

    /**
     * Sets a warp, will not overwrite current warps.
     *
     * @param warpName The name of the warp to set.
     * @param location The location of the warp.
     * @return <code>true</code> if set, <code>false</code> otherwise.
     */
    boolean setWarp(String warpName, Location<World> location);

    /**
     * Gets the names of all the warps that are available.
     *
     * @return A set of warps.
     */
    Set<String> getWarpNames();

    /**
     * Gets whether a warp exists.
     *
     * @param name The name to check for.
     * @return <code>true</code> if it exists, <code>false</code> otherwise.
     */
    boolean warpExists(String name);
}
