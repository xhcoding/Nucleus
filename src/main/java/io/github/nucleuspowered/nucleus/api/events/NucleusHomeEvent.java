/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.events;

import io.github.nucleuspowered.nucleus.api.Stable;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.user.TargetUserEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * Events when a player's home changes. Target user is the user being warped.
 */
@Stable
public interface NucleusHomeEvent extends Cancellable, CancelMessage {

    /**
     * Gets the name of the home.
     *
     * @return The name of the home.
     */
    String getName();

    /**
     * Gets the owner of the house
     *
     * @return The owner.
     */
    User getOwner();

    /**
     * Fired when a home is created.
     */
    interface Create extends NucleusHomeEvent {

        /**
         * Gets the proposed {@link Location} of the home.
         *
         * @return The location.
         */
        Location<World> getNewHomeLocation();
    }

    /**
     * Fired when a home is moved.
     */
    interface Modify extends NucleusHomeEvent {

        /**
         * Gets the current {@link Location} of the home.
         *
         * @return The location. It might not exist if the world does not exist any more.
         */
        Optional<Location<World>> getHomeLocation();

        /**
         * Gets the proposed {@link Location} of the home.
         *
         * @return The location.
         */
        Location<World> getNewHomeLocation();
    }

    /**
     * Fired when a home is deleted.
     */
    interface Delete extends NucleusHomeEvent {

        /**
         * Gets the {@link Location} of the home.
         *
         * @return The location. It might not exist if the world does not exist any more.
         */
        Optional<Location<World>> getHomeLocation();
    }

    interface Use extends NucleusHomeEvent, TargetUserEvent {

        /**
         * Gets the {@link Location} of the home.
         *
         * @return The location.
         */
        Location<World> getHomeLocation();
    }
}
