/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.interfaces;

import io.github.nucleuspowered.nucleus.api.data.JailData;
import io.github.nucleuspowered.nucleus.api.data.NucleusUser;
import io.github.nucleuspowered.nucleus.api.data.mail.MailData;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.Instant;
import java.util.List;
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
}
