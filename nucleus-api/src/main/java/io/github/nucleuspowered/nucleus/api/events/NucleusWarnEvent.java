/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.events;

import org.spongepowered.api.event.user.TargetUserEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.util.Optional;

/**
 * Events for when players are warned.
 */
@NonnullByDefault
public interface NucleusWarnEvent extends TargetUserEvent {

    /**
     * Fired when a player has been warned.
     */
    interface Warned extends NucleusWarnEvent {

        /**
         * The reason for the warning.
         *
         * @return The reason
         */
        String getReason();

        /**
         * If applicable, how long until the warning expires.
         *
         * @return The time until expiry for the warning.
         */
        Optional<Duration> getTimeUntilExpiration();
    }
}
