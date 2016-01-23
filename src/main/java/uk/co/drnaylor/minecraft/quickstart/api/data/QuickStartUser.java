package uk.co.drnaylor.minecraft.quickstart.api.data;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import uk.co.drnaylor.minecraft.quickstart.api.data.mute.MuteData;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents data held about a user in {@link uk.co.drnaylor.minecraft.quickstart.QuickStart}.
 */
public interface QuickStartUser {

    /**
     * Returns the user associated with this data.
     *
     * @return The {@link User}.
     */
    @NonnullByDefault User getUser();

    /**
     * Returns the {@link UUID} of the user (shorthand for {@link User#getUniqueId()}.
     *
     * @return The {@link UUID}
     */
    @NonnullByDefault UUID getUniqueID();

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
