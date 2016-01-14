package uk.co.drnaylor.minecraft.quickstart.api.service;

import org.spongepowered.api.entity.living.player.User;
import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.NoSuchPlayerException;

import java.util.UUID;

/**
 * A service that retrieves {@link QuickStartUser}s.
 */
public interface QuickStartUserService {

    /**
     * Gets the user data file from their UUID.
     *
     * @param playerUUID The {@link UUID} of the player in question.
     * @return The {@link QuickStartUser}
     */
    QuickStartUser getUser(UUID playerUUID) throws NoSuchPlayerException;

    /**
     * Gets the user data file from the {@link User}.
     *
     * @param user The {@link User} of the player in question.
     * @return The {@link QuickStartUser}
     */
    QuickStartUser getUser(User user);
}
