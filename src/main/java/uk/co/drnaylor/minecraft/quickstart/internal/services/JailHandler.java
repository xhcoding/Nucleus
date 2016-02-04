package uk.co.drnaylor.minecraft.quickstart.internal.services;

import com.flowpowered.math.vector.Vector3d;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.data.JailData;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartJailService;
import uk.co.drnaylor.minecraft.quickstart.config.WarpsConfig;
import uk.co.drnaylor.minecraft.quickstart.internal.ConfigMap;
import uk.co.drnaylor.minecraft.quickstart.internal.interfaces.InternalQuickStartUser;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JailHandler implements QuickStartJailService {

    private final QuickStart plugin;

    public JailHandler(QuickStart plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean setJail(String name, Location<World> location, Vector3d rotation) {
        return plugin.getConfig(ConfigMap.JAILS_CONFIG).get().setWarp(name, location, rotation);
    }

    @Override
    public Map<String, WarpLocation> getJails() {
        final WarpsConfig jc = plugin.getConfig(ConfigMap.JAILS_CONFIG).get();
        Map<String, WarpLocation> l = new HashMap<>();
        jc.getWarpNames().forEach(x -> jc.getWarp(x.toLowerCase()).ifPresent(y -> l.put(x.toLowerCase(), y)));
        return l;
    }

    @Override
    public Optional<WarpLocation> getJail(String name) {
        return plugin.getConfig(ConfigMap.JAILS_CONFIG).get().getWarp(name.toLowerCase());
    }

    @Override
    public boolean removeJail(String name) {
        return plugin.getConfig(ConfigMap.JAILS_CONFIG).get().removeWarp(name);
    }

    @Override
    public boolean isPlayerJailed(User user) {
        return getPlayerJailData(user).isPresent();
    }

    @Override
    public Optional<JailData> getPlayerJailData(User user) {
        try {
            return plugin.getUserLoader().getUser(user).getJailData();
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public boolean jailPlayer(User user, JailData data) {
        InternalQuickStartUser iqsu;
        try {
            iqsu = plugin.getUserLoader().getUser(user);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return false;
        }

        if (iqsu.getJailData().isPresent()) {
            return false;
        }

        // Get the jail.
        Optional<WarpLocation> owl = getJail(data.getJailName());
        WarpLocation wl = owl.orElseGet(() -> {
            if (!getJails().isEmpty()) {
                return null;
            }

            return getJails().entrySet().stream().findFirst().get().getValue();
        });

        if (wl == null) {
            return false;
        }

        iqsu.setJailData(data);
        if (user.isOnline()) {
            Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> {
                Player player = user.getPlayer().get();
                player.setLocationAndRotation(owl.get().getLocation(), owl.get().getRotation());
                iqsu.setFlying(false);
            });
        } else {
            iqsu.setJailOnNextLogin(true);
        }

        return true;
    }

    @Override
    public boolean unjailPlayer(User user) {
        InternalQuickStartUser iqsu;
        try {
            iqsu = plugin.getUserLoader().getUser(user);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return false;
        }

        Optional<JailData> ojd = iqsu.getJailData();
        if (!ojd.isPresent()) {
            return false;
        }

        Optional<Location<World>> ow = ojd.get().getPreviousLocation();
        iqsu.removeJailData();
        if (user.isOnline()) {
            Player player = user.getPlayer().get();
            Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> {
                player.setLocation(ow.isPresent() ? ow.get() : player.getWorld().getSpawnLocation());
                player.sendMessage(Text.of(TextColors.GREEN, Util.messageBundle.getString("jail.elapsed")));
            });
        } else {
            iqsu.sendToLocationOnLogin(ow.isPresent() ? ow.get() : new Location<>(Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorld().get().getUniqueId()).get(),
                    Sponge.getServer().getDefaultWorld().get().getSpawnPosition()));
        }

        return true;
    }

    public Optional<WarpLocation> getWarpLocation(Player user) {
        if (!isPlayerJailed(user)) {
            return Optional.empty();
        }

        Optional<WarpLocation> owl = getJail(getPlayerJailData(user).get().getJailName());
        if (!owl.isPresent()) {
            Collection<WarpLocation> wl = getJails().values();
            if (wl.isEmpty()) {
                return Optional.empty();
            }

            owl = wl.stream().findFirst();
        }

        return owl;
    }
}
