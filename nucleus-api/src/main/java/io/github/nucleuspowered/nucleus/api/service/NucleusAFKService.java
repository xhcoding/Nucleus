/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

import io.github.nucleuspowered.nucleus.api.Stable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Allows plugins to see a player's AFK status.
 */
@Stable
public interface NucleusAFKService {

    /**
     * Returns whether the {@link User} in question can go AFK.
     *
     * @param user The {@link User} in question
     * @return Whether or not the player can go AFK.
     */
    boolean canGoAFK(User user);

    /**
     * Returns whether a player is AFK
     *
     * @param player The player in question.
     * @return Whether the player is AFK.
     */
    boolean isAFK(Player player);

    /**
     * Sets a player's AFK status, if the player can go AFK.
     *
     * @param cause The cause for going AFK. The root cause must be a {@link PluginContainer}.
     * @param player The player to set the status of.
     * @param isAfk Whether the player should go AFK.
     *
     * @return <code>true</code> if successful, otherwise <code>false</code>, usually because the player is exempt from going AFK.
     */
    boolean setAFK(Cause cause, Player player, boolean isAfk);

    /**
     * Returns whether a {@link User} can be kicked for inactivity.
     *
     * @param user The {@link User} in question.
     * @return Whether the player can be kicked.
     */
    boolean canBeKicked(User user);

    /**
     * Returns the last recorded active moment of the player.
     *
     * @param player The player in question
     * @return The {@link Instant}
     */
    Instant lastActivity(Player player);

    /**
     * Returns how long the specified {@link User} has to be inactive before going AFK.
     *
     * @param user The {@link User} in question.
     * @return The {@link Duration}, or {@link Optional#empty()} if the player cannot go AFK.
     */
    Optional<Duration> timeForInactivity(User user);

    /**
     * Returns how long the specified {@link User} has to be inactive before being kicked.
     *
     * @param user The {@link User} in question.
     * @return The {@link Duration}, or {@link Optional#empty()} if the player cannot go AFK.
     */
    Optional<Duration> timeForKick(User user);

    /**
     * Invalidates cached permissions, used to resync a player's exemption status.
     */
    void invalidateCachedPermissions();
}
