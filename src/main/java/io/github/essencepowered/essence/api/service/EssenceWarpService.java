/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.api.service;

import com.flowpowered.math.vector.Vector3d;
import io.github.essencepowered.essence.api.data.WarpLocation;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.Set;

/**
 * Gets a service that allows users to warp about using defined warp.
 */
public interface EssenceWarpService {

    /**
     * Gets the location for the specified warp.
     *
     * @param warpName The name of the warp to check.
     * @return The {@link Location} of the warp, or {@link Optional#empty()} otherwise.
     */
    Optional<WarpLocation> getWarp(String warpName);

    /**
     * Removes a warp.
     *
     * @param warpName The name of the warp.
     * @return <code>true</code> if the warp was there and removed, <code>false</code> if the warp never existed.
     */
    boolean removeWarp(String warpName);

    /**
     * Sets a warp, will not overwrite current warp.
     *
     * @param warpName The name of the warp to set.
     * @param location The location of the warp.
     * @param rotation The rotation of the warp.
     * @return <code>true</code> if set, <code>false</code> otherwise.
     */
    boolean setWarp(String warpName, Location<World> location, Vector3d rotation);

    /**
     * Gets the names of all the warp that are available.
     *
     * @return A set of warp.
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
