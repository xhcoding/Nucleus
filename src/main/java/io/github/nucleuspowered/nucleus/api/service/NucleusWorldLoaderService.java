/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

import io.github.nucleuspowered.nucleus.api.data.NucleusWorld;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

/**
 * A service that retrieves {@link NucleusWorld}s.
 */
public interface NucleusWorldLoaderService {

    /**
     * Gets the world associated with the provided UUID.
     * @param uuid The {@link UUID} of the world to load.
     * @return The {@link NucleusWorld} that contains the Nucleus data for the world.
     */
    Optional<NucleusWorld> getWorld(UUID uuid);

    /**
     * Gets the world associated with the provided UUID.
     * @param world The {@link World} to load.
     * @return The {@link NucleusWorld} that contains the Nucleus data for the world.
     */
    Optional<NucleusWorld> getWorld(World world);

    /**
     * Saves all world data.
     */
    void saveAll();
}
