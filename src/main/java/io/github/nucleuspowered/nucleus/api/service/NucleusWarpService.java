/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

import com.flowpowered.math.vector.Vector3d;
import io.github.nucleuspowered.nucleus.api.Stable;
import io.github.nucleuspowered.nucleus.api.data.Warp;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 * Gets a service that allows users to warp about using defined warp.
 */
@NonnullByDefault
public interface NucleusWarpService {

    /**
     * Gets the location for the specified warp.
     *
     * @param warpName The name of the warp to check.
     * @return The {@link Location} of the warp, or {@link Optional#empty()} otherwise.
     */
    @Stable
    Optional<Warp> getWarp(String warpName);

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
     * Gets all the warps available.
     *
     * <p>If you just want to display the names, use {@link #getWarpNames()} instead.</p>
     *
     * @return All warps in Nucleus.
     */
    @Stable
    List<Warp> getAllWarps();

    /**
     * Get all warps that have not been given a category.
     *
     * @return The {@link Warp}s without a category.
     */
    @Stable
    List<Warp> getUncategorisedWarps();

    /**
     * Gets all warps that hae been given the specified category.
     *
     * @param category The category.
     * @return The warps.
     */
    @Stable
    List<Warp> getWarpsForCategory(String category);

    /**
     * Gets all warps that have categories.
     *
     * @return The warps.
     */
    @Stable
    Map<String, List<Warp>> getCategorisedWarps();

    /**
     * Gets all warps that have categories.
     *
     * @param warpDataPredicate The filtering predicate to return the subset of warps required.
     * @return The warps.
     */

    @Stable
    Map<String, List<Warp>> getCategorisedWarps(Predicate<Warp> warpDataPredicate);

    boolean removeWarpCost(String warpName);

    /**
     * Sets the cost of a warp.
     *
     * @param warpName The name of the warp to change the cost of.
     * @param cost The cost to use the warp. Set to zero (or negative) to disable.
     * @return <code>true</code> if the cost is set, <code>false</code> otherwise.
     */
    boolean setWarpCost(String warpName, double cost);

    /**
     * Sets a warp's category
     *
     * @param warpName The name of the warp.
     * @param category The name of the category.
     * @return {@code true} if successful
     */
    boolean setWarpCategory(String warpName, @Nullable String category);

    /**
     * Gets the names of all the warp that are available.
     *
     * @return A set of warp.
     */
    @Stable
    Set<String> getWarpNames();

    /**
     * Gets whether a warp exists.
     *
     * @param name The name to check for.
     * @return <code>true</code> if it exists, <code>false</code> otherwise.
     */
    @Stable
    default boolean warpExists(String name) {
        return getWarp(name).isPresent();
    }
}
