/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config.loaders;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.data.NucleusUser;
import io.github.nucleuspowered.nucleus.api.exceptions.NoSuchPlayerException;
import io.github.nucleuspowered.nucleus.api.service.NucleusUserLoaderService;
import io.github.nucleuspowered.nucleus.config.UserService;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.Identifiable;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserConfigLoader extends AbstractDataLoader<UUID, UserService> implements NucleusUserLoaderService {

    private final Map<UUID, SoftReference<UserService>> softLoadedUsers = Maps.newHashMap();

    public UserConfigLoader(Nucleus plugin) {
        super(plugin);
    }

    @Override
    public List<NucleusUser> getOnlineUsers() {
        return getOnlineUsersInternal().stream().map(x -> (NucleusUser)x).collect(Collectors.toList());
    }

    public List<InternalNucleusUser> getOnlineUsersInternal() {
        return Sponge.getServer().getOnlinePlayers().stream().map(x -> {
            try {
                return getUser(x);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }).filter(x -> x != null).collect(Collectors.toList());
    }

    @Override
    public InternalNucleusUser getUser(UUID playerUUID) throws Exception {
        return getUser(Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(playerUUID).orElseThrow(NoSuchPlayerException::new));
    }

    @Override
    public InternalNucleusUser getUser(User user) throws Exception {
        if (loaded.containsKey(user.getUniqueId())) {
            return loaded.get(user.getUniqueId());
        }

        // If we have a soft reference, move them into the strong reference queue before returning them.
        // The act of moving back into the soft loaded queue is a save. Soft loaded references are really
        // just acting as a cache.
        clearNullSoftReferences();
        if (softLoadedUsers.containsKey(user.getUniqueId())) {
            UserService uc = softLoadedUsers.get(user.getUniqueId()).get();
            softLoadedUsers.remove(user.getUniqueId());
            loaded.put(user.getUniqueId(), uc);
            return uc;
        }

        // Load the file in.
        UserService uc = new UserService(plugin, getUserPath(user.getUniqueId()), user);
        loaded.put(user.getUniqueId(), uc);
        return uc;
    }

    public void clearSoftReferenceCache() {
        softLoadedUsers.clear();
    }

    public void purgeNotOnline() {
        Set<UUID> onlineUUIDs = Sponge.getServer().getOnlinePlayers().stream().map(Identifiable::getUniqueId).collect(Collectors.toSet());

        // Collector to list should prevent CMEs.
        loaded.keySet().stream().filter(x -> !onlineUUIDs.contains(x)).collect(Collectors.toList()).forEach(x -> {
            try {
                UserService uc = loaded.get(x);
                uc.save();
                loaded.remove(x);
                softLoadedUsers.put(x, new SoftReference<>(uc));
            } catch (IOException | ObjectMappingException e) {
                plugin.getLogger().error("Could not save data for " + x.toString());
                e.printStackTrace();
            }
        });
    }

    public void forceUnloadPlayerWithoutSaving(UUID uuid) {
        purgeNotOnline();
        if (loaded.containsKey(uuid)) {
            // Really is no need to save at this point.
            loaded.remove(uuid);
        }

        if (softLoadedUsers.containsKey(uuid)) {
            softLoadedUsers.remove(uuid);
        }
    }

    public InternalNucleusUser getUserUnchecked(User user) {
        try {
            return getUser(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void clearNullSoftReferences() {
        // Remove any offline users that have ended up being GCd.
        softLoadedUsers.entrySet().removeIf(k -> k.getValue().get() == null);
    }

    public Path getUserPath(UUID uuid) throws IOException {
        String u = uuid.toString();
        String f = u.substring(0, 2);
        Path file = plugin.getDataPath().resolve(String.format("userdata%1$s%2$s%1$s%3$s.json", File.separator, f, u));

        if (Files.notExists(file)) {
            Files.createDirectories(file.getParent());
        }

        // Configurate will create it for us.
        return file;
    }
}
