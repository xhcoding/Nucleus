/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.back.handlers;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusBackService;
import io.github.nucleuspowered.nucleus.config.loaders.UserConfigLoader;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.InternalServiceManager;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import io.github.nucleuspowered.nucleus.modules.back.commands.BackCommand;
import io.github.nucleuspowered.nucleus.modules.back.config.BackConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.back.listeners.BackListeners;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

@SuppressWarnings("deprecation")
public class BackHandler implements NucleusBackService {

    @Inject private UserConfigLoader loader;
    @Inject private CoreConfigAdapter cca;

    // Temporary
    @Deprecated @Inject private BackConfigAdapter bca;
    @Deprecated @Inject private PermissionRegistry permissionRegistry;
    @Deprecated @Inject private Nucleus plugin;
    @Deprecated @Inject private InternalServiceManager serviceManager;

    @Deprecated
    private CommandPermissionHandler s = null;

    @Deprecated
    private CommandPermissionHandler getPermissionUtil() {
        if (s == null) {
            s = permissionRegistry.getService(BackCommand.class).orElseGet(() -> new CommandPermissionHandler(new BackCommand(), plugin));
        }

        return s;
    }

    @Deprecated
    private boolean getLogBackInternal(User player) {
        Optional<JailHandler> service = serviceManager.getService(JailHandler.class);
        return service.isPresent() && !service.get().isPlayerJailed(player) && getLogBack(player);
    }

    /**
     * <strong>DO NOT USE THIS METHOD EXTERNALLY.</strong>
     *
     * <p>
     * This method is really a clone of {@link NucleusBackService#setLastLocation(User, Transform)}, with an added permission check. This method is intended as a replacement
     * method to call when the teleport event is supposed to be fired.
     * </p>
     *
     * @deprecated Waiting for the teleport event to be fully implemented - this will be removed when this is so.
     * @param user The {@link User}
     * @param location The {@link Location}
     */
    @Deprecated
    public void setLastLocationInternal(User user, Transform<World> location) {
        if (bca.getNodeOrDefault().isOnTeleport() && getLogBackInternal(user) && getPermissionUtil().testSuffix(user, BackListeners.onTeleport)) {
            setLastLocation(user, location);
        }
    }
    // End Temporary

    @Override
    public Optional<Transform<World>> getLastLocation(User user) {
        Optional<InternalNucleusUser> oi = getUser(user);
        if (oi.isPresent()) {
            return oi.get().getLastLocation();
        }

        return Optional.empty();
    }

    @Override
    public void setLastLocation(User user, Transform<World> location) {
        getUser(user).ifPresent(x -> x.setLastLocation(location));
    }

    @Override
    public void removeLastLocation(User user) {
        getUser(user).ifPresent(x -> x.setLastLocation(null));
    }

    @Override
    public boolean getLogBack(User user) {
        Optional<InternalNucleusUser> oi = getUser(user);
        return oi.isPresent() && oi.get().isLogLastLocation();
    }

    @Override
    public void setLogBack(User user, boolean log) {
        getUser(user).ifPresent(x -> x.setLogLastLocation(log));
    }

    private Optional<InternalNucleusUser> getUser(User user) {
        try {
            return Optional.of(loader.getUser(user));
        } catch (Exception e) {
            if (cca.getNodeOrDefault().isDebugmode()) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }
}
