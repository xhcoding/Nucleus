/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.events;

import io.github.nucleuspowered.nucleus.api.Stable;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.user.TargetUserEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * Events when a server warp changes.
 */
@Stable
public interface NucleusWarpEvent extends Cancellable, CancelMessage {

    /**
     * Get the name of the warp.
     *
     * @return The name of the warp.
     */
    String getName();

    /**
     * Fired when a warp is created.
     */
    interface Create extends NucleusWarpEvent {

        /**
         * Gets the proposed {@link Location} of the warp.
         *
         * @return The location.
         */
        Location<World> getNewLocation();
    }

    /**
     * Fired when a warp is deleted.
     */
    interface Delete extends NucleusWarpEvent {

        /**
         * Gets the {@link Location} of the warp.
         *
         * @return The location. It might not exist if the world does not exist any more.
         */
        Optional<Location<World>> getLocation();
    }

    interface Use extends TargetUserEvent, NucleusWarpEvent {

        /**
         * Gets the {@link Location} of the warp.
         *
         * @return The location.
         */
        Location<World> getLocation();
    }
}
