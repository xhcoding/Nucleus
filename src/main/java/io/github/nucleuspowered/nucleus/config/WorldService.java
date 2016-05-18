/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.data.NucleusWorld;
import io.github.nucleuspowered.nucleus.config.bases.AbstractSerialisableClassConfig;
import io.github.nucleuspowered.nucleus.config.serialisers.WorldDataNode;
import io.github.nucleuspowered.nucleus.config.typeserialisers.Vector3dTypeSerialiser;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.spongepowered.api.world.World;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WorldService extends AbstractSerialisableClassConfig<WorldDataNode, ConfigurationNode, GsonConfigurationLoader> implements NucleusWorld {

    private static final Map<TypeToken<?>, TypeSerializer<?>> serializerMap;

    static {
        serializerMap = Maps.newHashMap();
        serializerMap.put(TypeToken.of(Vector3d.class), new Vector3dTypeSerialiser());
    }

    private final Nucleus plugin;

    private final UUID world;

    public WorldService(Nucleus plugin, Path worldPath, World world) throws Exception {
        super(worldPath, TypeToken.of(WorldDataNode.class), WorldDataNode::new, true, serializerMap);
        Preconditions.checkNotNull(world);
        Preconditions.checkNotNull(plugin);
        this.plugin = plugin;
        this.world = world.getUniqueId();
        load();
    }

    public UUID getUniqueID() {
        return world;
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
    public Optional<Vector3d> getSpawnRotation() {
        return data.getSpawnRotation();
    }

    @Override
    public void setSpawnRotation(Vector3d rotation) {
        data.setSpawnRotation(rotation);
    }

    @Override
    public void clearSpawnRotation() {
        data.setSpawnRotation(null);
    }

    @Override
    protected GsonConfigurationLoader getLoader(Path file, Map<TypeToken<?>, TypeSerializer<?>> typeSerializerList) {
        GsonConfigurationLoader.Builder gsb = GsonConfigurationLoader.builder();

        if (!typeSerializerList.isEmpty()) {
            ConfigurationOptions co = gsb.getDefaultOptions();
            TypeSerializerCollection tsc = co.getSerializers();
            typeSerializerList.forEach((t, s) -> {
                // Normal unsafe magic.
                tsc.registerType((TypeToken)t, (TypeSerializer)s);
            });

            co.setSerializers(tsc);
            gsb.setDefaultOptions(co);
        }

        return gsb.setPath(file).build();
    }

    @Override
    protected ConfigurationNode getNode() {
        return SimpleConfigurationNode.root();
    }
}
