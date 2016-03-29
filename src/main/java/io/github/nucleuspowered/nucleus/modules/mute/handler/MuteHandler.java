/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.handler;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.data.MuteData;
import io.github.nucleuspowered.nucleus.api.service.NucleusMuteService;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import org.spongepowered.api.entity.living.player.User;

import java.time.Instant;
import java.util.Optional;

public class MuteHandler implements NucleusMuteService {

    private final Nucleus nucleus;
    @Inject private UserConfigLoader ucl;

    public MuteHandler(Nucleus nucleus) {
        this.nucleus = nucleus;
    }

    @Override
    public boolean isMuted(User user) {
        return getPlayerMuteData(user).isPresent();
    }

    @Override
    public Optional<MuteData> getPlayerMuteData(User user) {
        Optional<InternalNucleusUser> nu = getNucleusUser(user);
        if (nu.isPresent()) {
            return nu.get().getMuteData();
        }

        return Optional.empty();
    }

    @Override
    public boolean mutePlayer(User user, MuteData data) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(data);

        Optional<InternalNucleusUser> nu = getNucleusUser(user);
        if (!nu.isPresent()) {
            return false;
        }

        InternalNucleusUser u = nu.get();
        if (user.isOnline() && data.getTimeFromNextLogin().isPresent() && !data.getEndTimestamp().isPresent()) {
            data = new MuteData(data.getMuter(), data.getReason(), Instant.now().plus(data.getTimeFromNextLogin().get()));
        }

        u.setMuteData(data);
        return true;
    }

    @Override
    public boolean unmutePlayer(User user) {
        if (isMuted(user)) {
            Optional<InternalNucleusUser> o = getNucleusUser(user);
            if (o.isPresent()) {
                o.get().removeMuteData();
                return true;
            }
        }

        return false;
    }

    private Optional<InternalNucleusUser> getNucleusUser(User user) {
        try {
            return Optional.of(ucl.getUser(user));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
