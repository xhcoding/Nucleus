/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.data.NucleusWorld;
import io.github.nucleuspowered.nucleus.config.bases.AbstractSerialisableClassConfig;
import io.github.nucleuspowered.nucleus.config.serialisers.WorldDataNode;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.api.world.World;

import java.nio.file.Path;
import java.util.UUID;

public class WorldService extends AbstractSerialisableClassConfig<WorldDataNode, ConfigurationNode, GsonConfigurationLoader> implements NucleusWorld {

    private final Nucleus plugin;

    // TODO: Think about whether we need the world object, or just the UUID.
    private final World world;

    public WorldService(Nucleus plugin, Path worldPath, World world) throws Exception {
        super(worldPath, TypeToken.of(WorldDataNode.class), WorldDataNode::new);
        Preconditions.checkNotNull(world);
        Preconditions.checkNotNull(plugin);
        this.plugin = plugin;
        this.world = world;
        load();
    }

    public World getWorld() {
        return world;
    }

    public UUID getUniqueID() {
        return world.getUniqueId();
    }

    @Override
    public boolean isLockWeather() {
        return data.isLockWeather();
    }

    @Override
    public void setLockWeather(boolean lockWeather) {
        data.setLockWeather(lockWeather);
    }

    @Override
    protected GsonConfigurationLoader getLoader(Path file) {
        return GsonConfigurationLoader.builder().setPath(file).build();
    }

    @Override
    protected ConfigurationNode getNode() {
        return SimpleConfigurationNode.root();
    }
}
