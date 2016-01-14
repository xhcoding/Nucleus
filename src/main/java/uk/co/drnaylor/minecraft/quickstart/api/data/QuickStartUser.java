package uk.co.drnaylor.minecraft.quickstart.api.data;

import org.spongepowered.api.entity.living.player.User;

/**
 * Represents data held about a user in {@link uk.co.drnaylor.minecraft.quickstart.QuickStart}.
 */
public interface QuickStartUser {

    /**
     * Returns the user associated with this data.
     *
     * @return The {@link User}.
     */
    User getUser();
}
