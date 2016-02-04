package uk.co.drnaylor.minecraft.quickstart.api.service;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import uk.co.drnaylor.minecraft.quickstart.api.data.JailData;
import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;

import java.util.Optional;
import java.util.Set;

/**
 * A service that handles player jailing.
 */
public interface QuickStartJailService {
    /**
     * Sets a jail location in the world.
     *
     * @param name The name of the jail to use.
     * @param location The {@link Location} in a world for the jail.
     * @param rotation The rotation of the player once in jail.
     * @return <code>true</code> if the creation of a jail point was a success.
     */
    boolean setJail(String name, Location<World> location, Vector3d rotation);

    /**
     * Gets the name of the jails on the server.
     *
     * @return A {@link Set} of names.
     */
    Set<String> getJails();

    /**
     * Gets the location of a jail, if it exists.
     *
     * @param name The name of the jail to get. Case in-sensitive.
     * @return An {@link Optional} that potentially contains the {@link WarpLocation} if the jail exists.
     */
    Optional<WarpLocation> getJail(String name);

    /**
     * Removes a jail location from the list.
     *
     * @param name The name of the jail to remove.
     * @return <code>true</code> if successful.
     */
    boolean removeJail(String name);

    /**
     * Returns whether a player is jailed.
     *
     * @param user The {@link User} to check.
     * @return <code>true</code> if the player is jailed.
     */
    boolean isPlayerJailed(User user);

    /**
     * Returns information about why a player is jailed, if they are indeed jailed.
     *
     * <p>
     *     This is equivalent to {@link QuickStartUser#getJailData()}
     * </p>
     *
     * @param user The {@link User} to check
     * @return An {@link Optional} that will contain {@link JailData} if the player is jailed.
     */
    Optional<JailData> getPlayerJailData(User user);

    /**
     * Jails a player if they are not currently jailed.
     *
     * @param user The {@link User} to jail.
     * @param data The {@link JailData} that contains information to jail with.
     * @return <code>true</code> if the player was jailed successfully.
     */
    boolean jailPlayer(User user, JailData data);

    /**
     * Unjails a player if they are currently jailed.
     *
     * @param user The {@link User} to unjail.
     * @return <code>true</code> if the player was unjailed successfully.
     */
    boolean unjailPlayer(User user);
}
