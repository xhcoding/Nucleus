/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.Kit;
import io.github.nucleuspowered.nucleus.api.data.LocationData;
import io.github.nucleuspowered.nucleus.api.data.WarpData;
import io.github.nucleuspowered.nucleus.api.exceptions.NoSuchWorldException;
import io.github.nucleuspowered.nucleus.configurate.datatypes.GeneralDataNode;
import io.github.nucleuspowered.nucleus.configurate.datatypes.KitDataNode;
import io.github.nucleuspowered.nucleus.configurate.datatypes.LocationNode;
import io.github.nucleuspowered.nucleus.configurate.datatypes.WarpNode;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class GeneralService extends Service<GeneralDataNode> {

    private final BiFunction<String, LocationNode, LocationData> getLocationData = (s, l) -> {
        try {
            return new LocationData(s, l.getLocation(), l.getRotation());
        } catch (NoSuchWorldException e) {
            return null;
        }
    };

    private final BiFunction<String, WarpNode, WarpData> getWarpLocation = (s, l) -> {
        try {
            return new WarpData(s, l.getLocation(), l.getRotation(), l.getCost());
        } catch (NoSuchWorldException e) {
            return null;
        }
    };

    public GeneralService(DataProvider<GeneralDataNode> provider) throws Exception {
        super(provider);
        provider.load();
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
        Map<String, KitDataNode> msk = data.getKits();
        Optional<String> key = Util.getKeyIgnoreCase(data.getKits(), name);
        if (key.isPresent()) {
            return Optional.of(msk.get(key.get()));
        }

        return Optional.empty();
    }

    public Map<String, Kit> getKits() {
        return ImmutableMap.copyOf(data.getKits());
    }

    public boolean addKit(String name, KitDataNode kit) {
        if (data.getKits().keySet().stream().anyMatch(name::equalsIgnoreCase)) {
            return false;
        }

        data.getKits().put(name, kit);
        return true;
    }

    public boolean removeKit(String name) {
        Map<String, KitDataNode> msk = data.getKits();
        Optional<String> key = msk.keySet().stream().filter(name::equalsIgnoreCase).findFirst();
        return key.isPresent() && data.getKits().remove(key.get()) != null;
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

    public Optional<LocationData> getJailLocation(String name) {
        return getLocation(name, data.getJails());
    }

    public Map<String, LocationData> getJails() {
        return getLocations(data.getJails());
    }

    public boolean addJail(String name, Location<World> loc, Vector3d rot) {
        return addLocation(name, loc, rot, data.getJails());
    }

    public boolean removeJail(String name) {
        return removeLocation(name, data.getJails());
    }

    public Optional<WarpData> getWarpLocation(String name) {
        return getLocation(name, data.getWarps(), getWarpLocation);
    }

    public Map<String, WarpData> getWarps() {
        return getLocations(data.getWarps(), getWarpLocation);
    }

    public boolean addWarp(String name, Location<World> loc, Vector3d rot) {
        Map<String, WarpNode> m = data.getWarps();
        if (Util.getKeyIgnoreCase(m, name).isPresent()) {
            return false;
        }

        m.put(name, new WarpNode(loc, rot));
        return true;
    }

    public boolean setWarpCost(String name, int cost) {
        Preconditions.checkArgument(cost >= -1);
        Map<String, WarpNode> m = data.getWarps();
        Optional<WarpNode> os = Util.getValueIgnoreCase(m, name);
        if (os.isPresent()) {
            // No need to put it back - it's saved automatically.
            os.get().setCost(cost);
            return true;
        }

        return false;
    }

    public boolean removeWarp(String name) {
        Map<String, WarpNode> m = data.getWarps();
        Optional<String> os = Util.getKeyIgnoreCase(m, name);
        if (os.isPresent()) {
            m.remove(os.get());
            return true;
        }

        return false;
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

    private Optional<LocationData> getLocation(String name, Map<String, LocationNode> m) {
        return getLocation(name, m, getLocationData);
    }

    private <T extends LocationData, S extends LocationNode> Optional<T> getLocation(String name, Map<String, S> m, BiFunction<String, S, T> converter) {
        Optional<S> o = Util.getValueIgnoreCase(m, name);
        if (!o.isPresent()) {
            return Optional.empty();
        }

        return Optional.ofNullable(converter.apply(name, o.get()));
    }

    private Map<String, LocationData> getLocations(Map<String, LocationNode> msl) {
        return getLocations(msl, getLocationData);
    }

    private <T extends LocationData, S extends LocationNode> Map<String, T> getLocations(Map<String, S> m, BiFunction<String, S, T> converter) {
        return m.entrySet().stream().map(x -> converter.apply(x.getKey(), x.getValue())).filter(x -> x != null).collect(Collectors.toMap(T::getName, x -> x));
    }

    private boolean addLocation(String name, Location<World> loc, Vector3d rot, Map<String, LocationNode> m) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(loc);
        Preconditions.checkNotNull(rot);
        if (Util.getKeyIgnoreCase(m, name).isPresent()) {
            return false;
        }

        m.put(name, new LocationNode(loc, rot));
        return true;
    }

    private boolean removeLocation(String name, Map<String, LocationNode> m) {
        Optional<Map.Entry<String, LocationNode>> o = m.entrySet().stream().filter(k -> k.getKey().equalsIgnoreCase(name)).findFirst();
        return o.isPresent() && m.remove(o.get().getKey()) != null;
    }
}
