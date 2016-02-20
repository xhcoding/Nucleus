/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.api.service;

import org.spongepowered.api.entity.living.player.User;
import uk.co.drnaylor.minecraft.quickstart.api.data.mail.BetweenInstantsData;
import uk.co.drnaylor.minecraft.quickstart.api.data.mail.MailData;
import uk.co.drnaylor.minecraft.quickstart.api.data.mail.MailFilter;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.NoSuchPlayerException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A service that handles sending and retrieving mail.
 */
public interface QuickStartMailService {

    /**
     * Gets mail for a specific player, optionally including a list of filters.
     *
     * @param player The {@link User} of the player to get the mail of.
     * @param filters The {@link MailFilter}s
     * @return A list of mail.
     */
    List<MailData> getMail(User player, MailFilter... filters);

    /**
     * Sends mail to a player, addressed from another player.
     *
     * @param playerFrom The {@link User} of the player to send the message from.
     * @param playerTo The {@link User} of the player to send the message to.
     * @param message The message.
     * @throws NoSuchPlayerException Thrown when either player has never played on the server.
     */
    void sendMail(User playerFrom, User playerTo, String message);

    /**
     * Sends mail to a player, addressed from the console.
     *
     * @param playerTo The {@link User} of the player to send the message to.
     * @param message The message.
     * @throws NoSuchPlayerException Thrown when the player has never played on the server.
     */
    void sendMailFromConsole(User playerTo, String message);

    /**
     * Clears the player's mail.
     *
     * @param player The {@link UUID} of the player.
     * @throws NoSuchPlayerException Thrown when the player has never played on the server.
     */
    void clearUserMail(User player);

    /**
     * Create a filter that restricts the mail to the senders provided.
     *
     * <p>
     *     Multiple player filters can be provided - this will return messages authored by all specified players.
     * </p>
     *
     * @param player The {@link UUID} of the player. Use <code>null</code> for the console.
     * @return The {@link MailFilter}
     * @throws NoSuchPlayerException Thrown when the player has never player on the server.
     */
    MailFilter<UUID> createPlayerFilter(UUID player) throws NoSuchPlayerException;

    /**
     * Create a filter that restricts the returned mail to the console.
     *
     * <p>
     *      This can be combined with the {@link #createPlayerFilter(UUID)} method to include players too.
     * </p>
     *
     * @return The {@link MailFilter}
     */
    MailFilter<Void> createConsoleFilter();

    /**
     * Create a filter that restricts the mail to a certain time period. One parameter may be
     * null, but not both. The times on the instants will be ignored.
     *
     * <p>
     *     Only <strong>one</strong> of these filters can be used at a time.
     * </p>
     *
     * @param from The {@link Instant} which indicates the earliest date to return.
     * @param to The {@link Instant} which indicates the latest date to return.
     * @return The {@link MailFilter}
     */
    MailFilter<BetweenInstantsData> createDateFilter(Instant from, Instant to);

    /**
     * Create a filter that restricts the messages returned to the provided substring.
     *
     * <p>
     *     If multiple filters are set, all need to match.
     * </p>
     *
     * @param message The message.
     * @return The {@link MailFilter}
     */
    MailFilter<String> createMessageFilter(String message);
}
