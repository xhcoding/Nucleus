/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.datamodules;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Warp;
import io.github.nucleuspowered.nucleus.configurate.datatypes.WarpNode;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataKey;
import io.github.nucleuspowered.nucleus.dataservices.modular.LocationDataModule;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularGeneralService;
import io.github.nucleuspowered.nucleus.internal.LocationData;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

public class WarpGeneralDataModule extends LocationDataModule<ModularGeneralService> {

    private final BiFunction<String, WarpNode, Warp> getWarpLocation = (s, l) ->
            new WarpData(s, l.getWorld(), l.getPosition(), l.getRotation(), l.getCost(), l.getCategory().orElse(null));

    @DataKey("warps")
    private Map<String, WarpNode> warps = Maps.newHashMap();

    public Optional<Warp> getWarpLocation(String name) {
        return get(warps, getWarpLocation, name);
    }

    public Map<String, Warp> getWarps() {
        return convert(warps, getWarpLocation);
    }

    public boolean addWarp(String name, Location<World> loc, Vector3d rot) {
        if (Util.getKeyIgnoreCase(warps, name).isPresent()) {
            return false;
        }

        warps.put(name, new WarpNode(loc, rot));
        return true;
    }

    public boolean setWarpCost(String name, double cost) {
        Preconditions.checkArgument(cost >= -1);
        Optional<WarpNode> os = Util.getValueIgnoreCase(warps, name);
        if (os.isPresent()) {
            // No need to put it back - it's saved automatically.
            os.get().setCost(cost);
            return true;
        }

        return false;
    }

    public boolean setWarpCategory(String name, @Nullable String category) {
        Optional<WarpNode> os = Util.getValueIgnoreCase(warps, name);
        if (os.isPresent()) {
            // No need to put it back - it's saved automatically.
            os.get().setCategory(category);
            return true;
        }

        return false;
    }

    public boolean removeWarp(String name) {
        Optional<String> os = Util.getKeyIgnoreCase(warps, name);
        if (os.isPresent()) {
            warps.remove(os.get());
            return true;
        }

        return false;
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
