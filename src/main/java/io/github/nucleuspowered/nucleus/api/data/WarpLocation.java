/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.data;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.text.MessageFormat;

public class WarpLocation {

    private final String warpName;
    protected final Location<World> location;
    private final Vector3d rotation;

    public WarpLocation(String name, Location<World> location, Vector3d rotation) {
        this.rotation = rotation;
        this.location = location;
        this.warpName = name;
    }

    public String getName() {
        return warpName;
    }

    public Vector3d getRotation() {
        return rotation;
    }

    public Location<World> getLocation() {
        return location;
    }

    public String toLocationString() {
        return MessageFormat.format("world: {0}, x: {1}, y: {2}, z: {3}", location.getExtent().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
