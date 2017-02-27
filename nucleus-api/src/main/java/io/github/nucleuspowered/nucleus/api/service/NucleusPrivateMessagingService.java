/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

import io.github.nucleuspowered.nucleus.api.Stable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.Tristate;

import java.util.Optional;
import java.util.Set;

/**
 * A service that contains message related APIs.
 */
@Stable
public interface NucleusPrivateMessagingService {

    /**
     * Returns whether the user is able to see all private messages sent on the server. This indicates that the user
     * has the correct permission AND has activated it.
     *
     * @param user The {@link User} to check.
     * @return <code>true</code> if the user has Social Spy enabled.
     */
    boolean isSocialSpy(User user);

    /**
     * Returns whether the server is using social spy levels.
     *
     * @return <code>true</code> if so.
     */
    boolean isUsingSocialSpyLevels();

    /**
     * If using social spy levels, this returns whether those on the same social spy level as the participants of a message can read a message.
     *
     * <p>
     *     In the following scenarios, Alice has a social spy level of 5, and Bob a social spy level of 10.
     * </p>
     * <p>
     *     If this method returns <code>true</code>, and Alice sends a message to Bob, Eve must have a level of 10 or above, to see the message. If
     *     Eve has a level of 9 or below, then because Bob's level is higher than hers, she <em>cannot</em> see the message.
     * </p>
     * <p>
     *     If this method returns <code>false</code>, and Alice sends a message to Bob, Eve must have a level of <strong>11</strong> or above, to see
     *     the message. If Eve has a level of 10 or below, then because Bob's level is <strong>the same</strong> than hers, she <em>cannot</em> see
     *     the message.
     * </p>
     *
     * @return Whether the server allows those with the same social spy level to spy on each other.
     */
    boolean canSpySameLevel();

    /**
     * Gets the social spy level the server is assigned. Typically (but not always) this is {@link Integer#MAX_VALUE}, or zero if levels aren't
     * enabled.
     *
     * @return The console/server's social spy level.
     */
    int getServerLevel();

    /**
     * Gets the social spy level the user is assigned. This is zero by default.
     *
     * @param user The user to check.
     * @return The level.
     */
    int getSocialSpyLevel(User user);

    /**
     * Returns whether the specified user can toggle social spy {@link Tristate#UNDEFINED}, or whether they are forced to use social spy {@link
     * Tristate#TRUE}, or do not have permission to do so {@link Tristate#FALSE}.
     *
     * @param user The use to check.
     * @return A {@link Tristate} that indicates what state the user might be forced into.
     */
    Tristate forcedSocialSpyState(User user);

    /**
     * Sets whether the user is able to see all private messages on the server. This method will return whether the
     * system has fulfilled the request.
     *
     * @param user The {@link User}
     * @param isSocialSpy <code>true</code> to turn Social Spy on, <code>false</code> otherwise.
     * @return <code>true</code> if the change was fulfilled, <code>false</code> if the user does not have permission
     */
    boolean setSocialSpy(User user, boolean isSocialSpy);

    /**
     * Returns whether the specified user can spy on <strong>all</strong> of the specified sources.
     *
     * <p>
     *     This will return <code>false</code> if the spying user is also in the list of users to spy on.
     * </p>
     *
     * @param spyingUser The user that will be spying.
     * @param sourcesToSpyOn The {@link CommandSource}s to spy upon.
     * @return <code>true</code> if the user can spy on <strong>all</strong> the users.
     * @throws IllegalArgumentException thrown if no {@link CommandSource}s are supplied.
     */
    boolean canSpyOn(User spyingUser, CommandSource... sourcesToSpyOn) throws IllegalArgumentException;

    /**
     * Returns the {@link CommandSource}s that are online and can spy on <strong>all</strong> of the specified sources.
     *
     * <p>
     *     This will not return players in the list of users to spy on.
     * </p>
     *
     * @param includeConsole Whether to include the console in the returned {@link Set}.
     * @param sourcesToSpyOn The {@link CommandSource}s to spy upon.
     * @return A {@link Set} of {@link CommandSource}s that can spy upon the specified users.
     * @throws IllegalArgumentException thrown if no {@link CommandSource}s are supplied.
     */
    Set<CommandSource> onlinePlayersCanSpyOn(boolean includeConsole, CommandSource... sourcesToSpyOn) throws IllegalArgumentException;

    /**
     * Sends a message as the sender to the receiver. Takes a string to mirror what the command would do.
     *
     * @param sender The sender.
     * @param receiver The reciever.
     * @param message The message to send.
     * @return <code>true</code> if the message was sent, <code>false</code> otherwise.
     */
    boolean sendMessage(CommandSource sender, CommandSource receiver, String message);

    /**
     * Gets the {@link CommandSource} that the console will reply to if <code>/r</code> is used, if any.
     *
     * @return The {@link CommandSource}.
     */
    Optional<CommandSource> getConsoleReplyTo();

    /**
     * Gets the {@link CommandSource} that the specified {@link User} will reply to if <code>/r</code> is used, if any.
     *
     * @param from The {@link User} to inspect.
     * @return The {@link CommandSource}.
     */
    Optional<CommandSource> getReplyTo(User from);

    /**
     * Sets the {@link CommandSource} that the specified {@link User} will reply to if <code>/r</code> is used.
     * @param user The {@link User} to modify.
     * @param toReplyTo The {@link CommandSource}.
     */
    void setReplyTo(User user, CommandSource toReplyTo);

    /**
     * Sets the {@link CommandSource} that the console will reply to if <code>/r</code> is used.
     * @param toReplyTo The {@link CommandSource}.
     */
    void setConsoleReplyTo(CommandSource toReplyTo);

    /**
     * Removes the {@link CommandSource} that the specified {@link User} will reply to if <code>/r</code> is used.
     * @param user The {@link User} to modify.
     */
    void clearReplyTo(User user);

    /**
     * Removes the {@link CommandSource} that the console will reply to if <code>/r</code> is used.
     */
    void clearConsoleReplyTo();
}
