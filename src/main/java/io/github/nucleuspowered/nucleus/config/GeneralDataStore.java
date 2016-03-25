/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.api.data.LocationWithRotation;
import io.github.nucleuspowered.nucleus.api.data.WarpLocation;
import io.github.nucleuspowered.nucleus.api.exceptions.NoSuchWorldException;
import io.github.nucleuspowered.nucleus.config.bases.AbstractSerialisableClassConfig;
import io.github.nucleuspowered.nucleus.config.serialisers.GeneralDataNode;
import io.github.nucleuspowered.nucleus.config.serialisers.KitDataNode;
import io.github.nucleuspowered.nucleus.config.serialisers.LocationNode;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class GeneralDataStore extends AbstractSerialisableClassConfig<GeneralDataNode, ConfigurationNode, ConfigurationLoader<ConfigurationNode>> {

    public GeneralDataStore(Path file) throws Exception {
        super(file, TypeToken.of(GeneralDataNode.class), GeneralDataNode::new);
    }

    @Override
    protected ConfigurationNode getNode() {
        return SimpleConfigurationNode.root();
    }

    @Override
    protected ConfigurationLoader<ConfigurationNode> getLoader(Path file) {
        return GsonConfigurationLoader.builder().setPath(file).build();
    }

    public List<ItemType> getBlacklistedTypes() {
        return ImmutableList.copyOf(data.getBlacklistedTypes());
    }

    public boolean addBlacklistedType(ItemType type) {
        List<ItemType> types = data.getBlacklistedTypes();
        if (!types.contains(type)) {
            types.add(type);
            return true;
        }

        return false;
    }

    public boolean removeBlacklistedType(ItemType type) {
        return data.getBlacklistedTypes().remove(type);
    }

    public Optional<KitDataNode> getKit(String name) {
        return Optional.ofNullable(data.getKits().get(name.toLowerCase()));
    }

    public Map<String, KitDataNode> getKits() {
        return ImmutableMap.copyOf(data.getKits());
    }

    public boolean addKit(String name, KitDataNode kit) {
        if (data.getKits().containsKey(name.toLowerCase())) {
            return false;
        }

        data.getKits().put(name.toLowerCase(), kit);
        return true;
    }

    public boolean removeKit(String name) {
        return data.getKits().remove(name.toLowerCase()) != null;
    }

    public Optional<WarpLocation> getJailLocation(String name) {
        return getLocation(name, data.getJails());
    }

    public Map<String, WarpLocation> getJails() {
        return getLocations(data.getJails());
    }

    public boolean addJail(String name, Location<World> loc, Vector3d rot) {
        return addLocation(name, loc, rot, data.getJails());
    }

    public boolean removeJail(String name) {
        return data.getJails().remove(name.toLowerCase()) != null;
    }

    public Optional<WarpLocation> getWarpLocation(String name) {
        return getLocation(name, data.getWarps());
    }

    public Map<String, WarpLocation> getWarps() {
        return getLocations(data.getWarps());
    }

    public boolean addWarp(String name, Location<World> loc, Vector3d rot) {
        return addLocation(name, loc, rot, data.getWarps());
    }

    public boolean removeWarp(String name) {
        return data.getWarps().remove(name.toLowerCase()) != null;
    }

    public Optional<LocationWithRotation> getFirstSpawn() {
        Optional<LocationNode> ln = data.getFirstSpawnLocation();
        if (ln.isPresent()) {
            try {
                LocationWithRotation lwr = new LocationWithRotation(ln.get().getLocation(), ln.get().getRotation());
                return Optional.of(lwr);
            } catch (NoSuchWorldException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public void setFirstSpawn(Location<World> location, Vector3d rot) {
        data.setFirstSpawnLocation(new LocationNode(location, rot));
    }

    public void removeFirstSpawn() {
        data.setFirstSpawnLocation(null);
    }

    // Helper methods for warp based systems

    private Optional<WarpLocation> getLocation(String name, Map<String, LocationNode> m) {
        LocationNode ln = m.get(name.toLowerCase());
        if (ln == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(new WarpLocation(name.toLowerCase(), ln.getLocation(), ln.getRotation()));
        } catch (NoSuchWorldException e) {
            return Optional.empty();
        }
    }

    private Map<String, WarpLocation> getLocations(Map<String, LocationNode> m) {
        Map<String, WarpLocation> l = Maps.newHashMap();
        m.forEach((k, v) -> {
            try {
                l.put(k.toLowerCase(), new WarpLocation(k.toLowerCase(), v.getLocation(), v.getRotation()));
            } catch (NoSuchWorldException e) {
            }
        });

        return l;
    }

    private boolean addLocation(String name, Location<World> loc, Vector3d rot, Map<String, LocationNode> m) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(loc);
        Preconditions.checkNotNull(rot);
        if (m.containsKey(name.toLowerCase())) {
            return false;
        }

        m.put(name.toLowerCase(), new LocationNode(loc, rot));
        return true;
    }
}
