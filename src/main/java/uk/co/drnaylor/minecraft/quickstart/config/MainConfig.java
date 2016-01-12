package uk.co.drnaylor.minecraft.quickstart.config;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;

public class MainConfig extends AbstractConfig<CommentedConfigurationNode, HoconConfigurationLoader> {

    public MainConfig(Path file) throws IOException {
        super(file);
    }

    @Override
    protected HoconConfigurationLoader getLoader(Path file) {
        return HoconConfigurationLoader.builder().setPath(file).build();
    }


}
