package uk.co.drnaylor.minecraft.quickstart.internal.services;

import com.google.common.collect.Maps;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.Identifiable;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;
import uk.co.drnaylor.minecraft.quickstart.api.exceptions.NoSuchPlayerException;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartUserService;
import uk.co.drnaylor.minecraft.quickstart.config.UserConfig;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserConfigLoader implements QuickStartUserService {

    private final QuickStart plugin;
    private final Map<UUID, UserConfig> loadedUsers = Maps.newHashMap();

    public UserConfigLoader(QuickStart plugin) {
        this.plugin = plugin;
    }

    @Override
    public QuickStartUser getUser(UUID playerUUID) throws NoSuchPlayerException {
        return null;
    }

    @Override
    public QuickStartUser getUser(User user) {
        if (loadedUsers.containsKey(user.getUniqueId())) {
            return loadedUsers.get(user.getUniqueId());
        }

        return null;
    }

    public void saveAll() {
        loadedUsers.values().forEach(c -> {
            try {
                c.save();
            } catch (IOException e) {
                plugin.getLogger().error("Could not save data for " + c.getUniqueId().toString());
                e.printStackTrace();
            }
        });
    }

    public void purgeNotOnline() {
        Set<UUID> onlineUUIDs = Sponge.getServer().getOnlinePlayers().stream().map(Identifiable::getUniqueId).collect(Collectors.toSet());
        loadedUsers.keySet().stream().filter(x -> !onlineUUIDs.contains(x)).forEach(x -> {
            try {
                UserConfig uc = loadedUsers.get(x);
                uc.save();
                loadedUsers.remove(x);
            } catch (IOException e) {
                plugin.getLogger().error("Could not save data for " + x.toString());
                e.printStackTrace();
            }
        });
    }
}
