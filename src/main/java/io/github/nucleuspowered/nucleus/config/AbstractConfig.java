/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractConfig<T extends ConfigurationNode, L extends ConfigurationLoader<T>> {

    protected final L loader;
    protected T node;

    protected AbstractConfig(Path file) throws IOException, ObjectMappingException {
        loader = getLoader(file);
        load();
    }

    public void save() throws IOException, ObjectMappingException {
        loader.save(node);
    }

    public void load() throws IOException, ObjectMappingException {
        node = loader.load();
        node.mergeValuesFrom(getDefaults());
        save();
    }

    protected abstract L getLoader(Path file);

    protected abstract T getDefaults();
}
