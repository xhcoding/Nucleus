package uk.co.drnaylor.minecraft.quickstart.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.api.entity.living.player.User;
import uk.co.drnaylor.minecraft.quickstart.api.data.QuickStartUser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class UserConfig extends AbstractConfig<ConfigurationNode, GsonConfigurationLoader> implements QuickStartUser {
    private final User user;

    public UserConfig(Path file, User user) throws IOException {
        super(file);
        this.user = user;
    }

    @Override
    protected GsonConfigurationLoader getLoader(Path file) {
        return GsonConfigurationLoader.builder().setPath(file).build();
    }

    @Override
    protected ConfigurationNode getDefaults() {
        return SimpleConfigurationNode.root();
    }

    public UUID getUniqueId() {
        return user.getUniqueId();
    }
}
