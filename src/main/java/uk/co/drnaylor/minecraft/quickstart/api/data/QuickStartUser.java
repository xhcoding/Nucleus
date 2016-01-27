package uk.co.drnaylor.minecraft.quickstart.api.data;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Instant;
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

    /**
     * Gets whether the player is a Social Spy.
     *
     * @return <code>true</code> if the player is a social spy, <code>false</code> otherwise.
     */
    boolean isSocialSpy();

    /**
     * Sets whether the player is a Social Spy. Note that they must also have the command permission (quickstart.socialspy.base).
     *
     * @param socialSpy <code>true</code> if they should be a spy.
     * @return <code>true</code> if the change was made.
     */
    boolean setSocialSpy(boolean socialSpy);

    /**
     * Gets whether QuickStart thinks the player should be invulnerable. Note, this means the player has been subject to
     * the /god command.
     *
     * @return <code>true</code> if so.
     */
    boolean isInvulnerable();

    /**
     * Sets whether QuickStart thinks the player should be invulnerable. Note, this is the same as the player being subject to
     * the /god command.
     *
     * @param invuln <code>true</code> if so, <code>false</code> otherwise.
     */
    void setInvulnerable(boolean invuln);

    /**
     * Gets the time the player last logged in.
     *
     * @return The {@link Instant}
     */
    Instant getLastLogin();

    /**
     * Gets the time the player last logged out.
     *
     * @return The {@link Instant}
     */
    Instant getLastLogout();
}
