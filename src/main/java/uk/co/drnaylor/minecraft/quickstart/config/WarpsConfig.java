package uk.co.drnaylor.minecraft.quickstart.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartWarpService;

import java.io.IOException;
import java.nio.file.Path;

public class WarpsConfig extends AbstractConfig<ConfigurationNode, GsonConfigurationLoader> implements QuickStartWarpService {

    protected WarpsConfig(Path file) throws IOException {
        super(file);
    }

    @Override
    protected GsonConfigurationLoader getLoader(Path file) {
        return GsonConfigurationLoader.builder().setPath(file).build();
    }

    @Override
    protected ConfigurationNode getDefaults() {
        return SimpleConfigurationNode.root();
    }
}
