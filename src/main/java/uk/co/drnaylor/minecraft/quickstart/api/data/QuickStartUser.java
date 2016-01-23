package uk.co.drnaylor.minecraft.quickstart.api.data;

import org.spongepowered.api.entity.living.player.User;
import uk.co.drnaylor.minecraft.quickstart.api.data.mute.MuteData;

import java.util.Optional;

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

    /**
     * Gets the {@link MuteData} associated with this user, if any.
     *
     * @return An {@link Optional} that might contain the mute data.
     */
    Optional<MuteData> getMuteData();

    /**
     * Sets the {@link MuteData} associated with this user, if any.
     *
     * @param data The {@link MuteData}
     */
    void setMuteData(MuteData data);

    /**
     * Removes the {@link MuteData} associated with this user.
     */
    void removeMuteData();
}
