/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.back.handlers;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.iapi.service.NucleusBackService;
import io.github.nucleuspowered.nucleus.modules.back.datamodules.BackUserTransientModule;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.World;

import java.util.Optional;

@SuppressWarnings("deprecation")
public class BackHandler implements NucleusBackService {

    @Inject private UserDataManager loader;
    @Inject private CoreConfigAdapter cca;

    @Override
    public Optional<Transform<World>> getLastLocation(User user) {
        Optional<ModularUserService> oi = getUser(user);
        if (oi.isPresent()) {
            return oi.get().getTransient(BackUserTransientModule.class).getLastLocation();
        }

        return Optional.empty();
    }

    @Override
    public void setLastLocation(User user, Transform<World> location) {
        getUser(user).ifPresent(x -> x.getTransient(BackUserTransientModule.class).setLastLocation(location));
    }

    @Override
    public void removeLastLocation(User user) {
        getUser(user).ifPresent(x -> x.getTransient(BackUserTransientModule.class).setLastLocation(null));
    }

    @Override
    public boolean getLogBack(User user) {
        Optional<ModularUserService> oi = getUser(user);
        return oi.isPresent() && oi.get().getTransient(BackUserTransientModule.class).isLogLastLocation();
    }

    @Override
    public void setLogBack(User user, boolean log) {
        getUser(user).ifPresent(x -> x.getTransient(BackUserTransientModule.class).setLogLastLocation(log));
    }

    private Optional<ModularUserService> getUser(User user) {
        try {
            return Optional.ofNullable(loader.get(user.getUniqueId()).orElse(null));
        } catch (Exception e) {
            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }
}
