/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
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
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GeneralDataStore extends AbstractSerialisableClassConfig<GeneralDataNode, ConfigurationNode, ConfigurationLoader<ConfigurationNode>> {

    public GeneralDataStore(Path file) throws Exception {
        super(file, TypeToken.of(GeneralDataNode.class), GeneralDataNode::new, false);
    }

    @Override
    protected ConfigurationNode getNode() {
        return SimpleConfigurationNode.root();
    }

    @Override
    protected ConfigurationLoader<ConfigurationNode> getLoader(Path file, Map<TypeToken<?>, TypeSerializer<?>> typeSerializerList) {
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

    public List<ItemStackSnapshot> getFirstKit() {
        return data.getFirstKit();
    }

    public void setFirstKit(@Nullable List<ItemStackSnapshot> stack) {
        if (stack == null) {
            stack = Lists.newArrayList();
        }

        data.setFirstKit(stack);
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
        return removeLocation(name, data.getJails());
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
        return removeLocation(name, data.getWarps());
    }

    public Optional<Transform<World>> getFirstSpawn() {
        Optional<LocationNode> ln = data.getFirstSpawnLocation();
        if (ln.isPresent()) {
            try {
                Transform<World> lwr = new Transform<>(ln.get().getLocation().getExtent(), ln.get().getLocation().getPosition(), ln.get().getRotation());
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
        Optional<Map.Entry<String, LocationNode>> o = m.entrySet().stream().filter(k -> k.getKey().equalsIgnoreCase(name)).findFirst();
        if (!o.isPresent()) {
            return Optional.empty();
        }

        LocationNode ln = o.get().getValue();
        try {
            return Optional.of(new WarpLocation(name, ln.getLocation(), ln.getRotation()));
        } catch (NoSuchWorldException e) {
            return Optional.empty();
        }
    }

    private Map<String, WarpLocation> getLocations(Map<String, LocationNode> m) {
        Map<String, WarpLocation> l = Maps.newHashMap();
        m.forEach((k, v) -> {
            try {
                l.put(k, new WarpLocation(k, v.getLocation(), v.getRotation()));
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

        m.put(name, new LocationNode(loc, rot));
        return true;
    }

    private boolean removeLocation(String name, Map<String, LocationNode> m) {
        Optional<Map.Entry<String, LocationNode>> o = m.entrySet().stream().filter(k -> k.getKey().equalsIgnoreCase(name)).findFirst();
        if (o.isPresent()) {
            return m.remove(o.get().getKey()) != null;
        }

        return false;
    }
}
