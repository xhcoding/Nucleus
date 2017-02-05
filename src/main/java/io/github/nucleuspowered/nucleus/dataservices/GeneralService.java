/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.exceptions.NoSuchWorldException;
import io.github.nucleuspowered.nucleus.api.nucleusdata.NamedLocation;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Warp;
import io.github.nucleuspowered.nucleus.configurate.datatypes.GeneralDataNode;
import io.github.nucleuspowered.nucleus.configurate.datatypes.LocationNode;
import io.github.nucleuspowered.nucleus.configurate.datatypes.WarpNode;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import io.github.nucleuspowered.nucleus.internal.LocationData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class GeneralService extends AbstractService<GeneralDataNode> {

    // This is a session variable - does not get saved on restart.
    private long userCount = 0;
    private boolean userCountIsDirty = false;

    private final BiFunction<String, LocationNode, NamedLocation> getLocationData = (s, l) ->
        new LocationData(s, l.getWorld(), l.getPosition(), l.getRotation());

    private final BiFunction<String, WarpNode, Warp> getWarpLocation = (s, l) ->
        new WarpData(s, l.getWorld(), l.getPosition(), l.getRotation(), l.getCost(), l.getCategory().orElse(null));

    public GeneralService(DataProvider<GeneralDataNode> provider) throws Exception {
        // This gets set up early, but we don't want to load it until post-init.
        super(provider, false);
    }

    public Optional<NamedLocation> getJailLocation(String name) {
        return get(data.getJails(), getLocationData, name);
    }

    public Map<String, NamedLocation> getJails() {
        return convert(data.getJails(), getLocationData);
    }

    public boolean addJail(String name, Location<World> loc, Vector3d rot) {
        return addLocation(name, loc, rot, data.getJails());
    }

    public boolean removeJail(String name) {
        return removeLocation(name, data.getJails());
    }

    public Optional<Warp> getWarpLocation(String name) {
        return get(data.getWarps(), getWarpLocation, name);
    }

    public Map<String, Warp> getWarps() {
        return convert(data.getWarps(), getWarpLocation);
    }

    public boolean addWarp(String name, Location<World> loc, Vector3d rot) {
        Map<String, WarpNode> m = data.getWarps();
        if (Util.getKeyIgnoreCase(m, name).isPresent()) {
            return false;
        }

        m.put(name, new WarpNode(loc, rot));
        return true;
    }

    public boolean setWarpCost(String name, double cost) {
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

    public boolean setWarpCategory(String name, @Nullable String category) {
        Map<String, WarpNode> m = data.getWarps();
        Optional<WarpNode> os = Util.getValueIgnoreCase(m, name);
        if (os.isPresent()) {
            // No need to put it back - it's saved automatically.
            os.get().setCategory(category);
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

    private <S extends LocationNode, T extends NamedLocation> Optional<T> get(Map<String, S> input, BiFunction<String, S, T> convert, String name) {
        return Util.getValueIgnoreCase(convert(input, convert), name);
    }

    private <S extends LocationNode, T extends NamedLocation> Map<String, T> convert(Map<String, S> input, BiFunction<String, S, T> convert) {
        return input.entrySet().stream()
            .map(x -> convert.apply(x.getKey(), x.getValue()))
            .filter(Objects::nonNull).collect(Collectors.toMap(T::getName, x -> x));
    }

    public void resetUniqueUserCount() {
        resetUniqueUserCount(null);
    }

    public void resetUniqueUserCount(@Nullable Consumer<Long> resultConsumer) {
        if (!this.userCountIsDirty) {
            this.userCountIsDirty = true;
            Task.builder().async().execute(task -> {
                UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);

                // This could be slow...
                this.userCount = uss.getAll().stream().filter(GameProfile::isFilled)
                    .map(uss::get).filter(Optional::isPresent)
                    .filter(x ->
                        x.get().getPlayer().isPresent() ||
                            Nucleus.getNucleus().getUserDataManager().has(x.get().getUniqueId()) ||
                            // Temporary until Data is hooked up properly, I hope.
                            x.get().get(JoinData.class).map(y -> y.firstPlayed().getDirect().isPresent()).orElse(false)).count();
                this.userCountIsDirty = false;
                if (resultConsumer != null) {
                    resultConsumer.accept(this.userCount);
                }
            }).submit(Nucleus.getNucleus());
        }
    }

    public long getUniqueUserCount() {
        if (this.userCountIsDirty) {
            return this.userCount + 1;
        }

        return this.userCount;
    }

    private static class WarpData extends LocationData implements Warp {

        private final double cost;
        private final String category;

        private WarpData(String name, UUID world, Vector3d position, Vector3d rotation, double cost, @Nullable String category) {
            super(name, world, position, rotation);
            this.cost = Math.max(-1, cost);
            this.category = category;
        }

        public Optional<Double> getCost() {
            if (cost > -1) {
                return Optional.of(cost);
            }

            return Optional.empty();
        }

        public Optional<String> getCategory() {
            return Optional.ofNullable(category);
        }

        @Override
        public String toString() {
            return super.toString() + "category: " + this.category + ", cost: " + this.cost;
        }
    }
}
