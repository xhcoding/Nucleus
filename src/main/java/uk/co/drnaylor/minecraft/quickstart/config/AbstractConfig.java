package uk.co.drnaylor.minecraft.quickstart.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public abstract class AbstractConfig<T extends ConfigurationNode, L extends ConfigurationLoader<T>> {

    protected final L loader;
    protected T node;

    protected AbstractConfig(Path file) throws IOException {
        loader = getLoader(file);
        load();
    }

    public void save() throws IOException {
        loader.save(node);
    }

    public void load() throws IOException {
        node = loader.load();
        node.mergeValuesFrom(getDefaults());
        save();
    }

    protected abstract L getLoader(Path file);

    protected abstract T getDefaults();
}
