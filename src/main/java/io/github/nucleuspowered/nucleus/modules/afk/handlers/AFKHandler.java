/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.handlers;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.NameUtil;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.modules.afk.commands.AFKCommand;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Identifiable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class AFKHandler {
    private final HashMap<UUID, AFKData> data = new HashMap<>();

    @Inject private Nucleus plugin;
    @Inject private AFKConfigAdapter aca;
    @Inject private PermissionRegistry permissionRegistry;

    private CommandPermissionHandler s = null;

    private CommandPermissionHandler getPermissionUtil() {
        if (s == null) {
            s = permissionRegistry.getService(AFKCommand.class).orElseGet(() -> new CommandPermissionHandler(new AFKCommand(), plugin));
        }

        return s;
    }

    public void login(UUID user) {
        getData(user);
    }

    /**
     * Updates activity.
     * @param user The user to update.
     *
     * @return <code>true</code> if the user has returned from AFK.
     */
    public boolean updateUserActivity(UUID user) {
        Optional<Player> pl = Sponge.getServer().getPlayer(user);
        if (pl.isPresent() && getPermissionUtil().testSuffix(pl.get(), "exempt.toggle")) {
            return false;
        }

        if (getData(user).update()) {
            sendMessage(user, false);
            return true;
        }

        return false;
    }

    public void setNoTrack(UUID user, boolean track) {
        getData(user).noTrack = track;
    }

    public void setAFK(UUID user, boolean afk) {
        Optional<Player> pl = Sponge.getServer().getPlayer(user);
        if (pl.isPresent() && getPermissionUtil().testSuffix(pl.get(), "exempt.toggle")) {
            return;
        }

        AFKData a = getData(user);
        a.isAFK = !a.noTrack && afk;

        sendMessage(user, afk);
    }

    public void purgeNotOnline() {
        List<UUID> uuid = Sponge.getServer().getOnlinePlayers().stream().map(Identifiable::getUniqueId).collect(Collectors.toList());

        // CMEs BE GONE! The collect part of this creates a new list, avoiding the CME.
        data.keySet().stream().filter(x -> !uuid.contains(x)).collect(Collectors.toList()).forEach(data::remove);
    }

    public AFKData getAFKData(Player pl) {
        return getData(pl.getUniqueId());
    }

    public List<UUID> checkForAfk(long amount) {
        Instant now = Instant.now();
        return data.entrySet().stream()
                .filter(x -> !x.getValue().noTrack && !x.getValue().isAFK && x.getValue().lastActivity.plus(amount, ChronoUnit.SECONDS).isBefore(now))
                .map(x -> {
                    setAFK(x.getKey(), true);
                    return x.getKey();
                }).collect(Collectors.toList());
    }

    public List<UUID> checkForAfkKick(long amount) {
        Instant now = Instant.now();
        return data.entrySet().stream()
                .filter(x -> !x.getValue().noTrack && x.getValue().lastActivity.plus(amount, ChronoUnit.SECONDS).isBefore(now))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private AFKData getData(UUID user) {
        if (!data.containsKey(user)) {
            AFKData a = new AFKData();
            data.put(user, a);
            return a;
        }

        return data.get(user);
    }

    private void sendMessage(UUID uuid, boolean afk) {
        Sponge.getServer().getPlayer(uuid).ifPresent(x -> {
            // If we have the config set to true, or the player is NOT invisible, send an AFK message
            if (aca.getNodeOrDefault().isAfkOnVanish() || !x.get(Keys.INVISIBLE).orElse(false)) {
                MessageChannel.TO_ALL
                        .send(Util.getTextMessageWithFormat(afk ? "afk.toafk" : "afk.fromafk", NameUtil.getSerialisedName(x)));
            }
        });
    }

    public static class AFKData {
        private boolean isAFK = false;
        private boolean noTrack = false;
        private Instant lastActivity = Instant.now();
        private final Object lockingObject = new Object();

        /**
         * Updates activity.
         * @return <code>true</code> if the user has returned from AFK.
         */
        private boolean update() {
            synchronized (lockingObject) {
                lastActivity = Instant.now();

                if (isAFK) {
                    this.isAFK = false;
                    return !noTrack;
                }

                return false;
            }
        }

        public boolean isAFK() {
            return isAFK;
        }

        public boolean notTracked() {
            return noTrack;
        }

        public Instant getLastActivity() {
            return lastActivity;
        }
    }
}
