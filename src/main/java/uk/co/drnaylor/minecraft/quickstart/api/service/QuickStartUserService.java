/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.api.service;

import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.User;
import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.NoSuchPlayerException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * A service that retrieves {@link QuickStartUser}s.
 */
public interface QuickStartUserService {

    /**
     * Gets a list of {@link QuickStartUser}s that represents the online.
     *
     * @return A {@link List} of {@link QuickStartUser}s.
     */
    List<QuickStartUser> getOnlineUsers();

    /**
     * Gets the user data file from their UUID.
     *
     * <p>
     *     Consumers of this API should note that this method handles some caching of {@link User} objects, and in most
     *     cases, will load {@link User} objects from memory, and will handle holding the object in memory after loading
     *     from the persistent data source. Consumers are therefore requested to NOT hold onto user objects in their
     *     code, preferring the UUID of the user so that an up to date object is returned should there be a requirement
     *     to reload the user from disc.
     * </p>
     *
     * @param playerUUID The {@link UUID} of the player in question.
     * @return The {@link QuickStartUser}
     */
    QuickStartUser getUser(UUID playerUUID) throws NoSuchPlayerException, IOException, ObjectMappingException;

    /**
     * Gets the user data file from the {@link User}.
     *
     * <p>
     *     Consumers of this API should note that this method handles some caching of {@link User} objects, and in most
     *     cases, will load {@link User} objects from memory, and will handle holding the object in memory after loading
     *     from the persistent data source. Consumers are therefore requested to NOT hold onto user objects in their
     *     code, preferring the UUID of the user so that an up to date object is returned should there be a requirement
     *     to reload the user from disc.
     * </p>
     *
     * @param user The {@link User} of the player in question.
     * @return The {@link QuickStartUser}
     */
    QuickStartUser getUser(User user) throws IOException, ObjectMappingException;
}
