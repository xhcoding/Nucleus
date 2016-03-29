/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.interfaces;

import io.github.nucleuspowered.nucleus.api.data.JailData;
import io.github.nucleuspowered.nucleus.api.data.MuteData;
import io.github.nucleuspowered.nucleus.api.data.NucleusUser;
import io.github.nucleuspowered.nucleus.api.data.mail.MailData;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InternalNucleusUser extends NucleusUser {

    void setLastLogin(Instant login);

    void removeLocationOnLogin();

    void setLastLogout(Instant logout);

    List<MailData> getMail();

    void addMail(MailData mailData);

    void clearMail();

    /**
     * Determines whether Nucleus thinks the player should be flying, but does not look at the current status of the
     * player. In other words, what did the data file say?
     *
     * @return <code>true</code> if so.
     */
    boolean isFlyingSafe();

    /**
     * Determines whether Nucleus thinks the player should be invulnerable, but does not look at the current status of the
     * player. In other words, what did the data file say?
     *
     * @return <code>true</code> if so.
     */
    boolean isInvulnerableSafe();

    void setJailData(JailData data);

    void removeJailData();

    void setOnLogout();

    Optional<Location<World>> getLocationOnLogin();

    void sendToLocationOnLogin(Location<World> worldLocation);

    boolean jailOnNextLogin();

    void setJailOnNextLogin(boolean set);

    Map<String, Instant> getKitLastUsedTime();

    void addKitLastUsedTime(String kitName, Instant lastTime);

    void removeKitLastUsedTime(String kitName);

    // -- Powertools
    Map<String, List<String>> getPowertools();

    void setPowertool(ItemType type, List<String> commands);

    void clearPowertool(ItemType type);

    void clearPowertool(String type);

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
