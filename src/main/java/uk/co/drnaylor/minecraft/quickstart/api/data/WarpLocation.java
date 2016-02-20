/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.api.data;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.text.MessageFormat;

public class WarpLocation {

    private final Location<World> location;

    private final Vector3d rotation;

    private final String warpName;

    public WarpLocation(String name, Location<World> location, Vector3d rotation) {
        this.warpName = name;
        this.location = location;
        this.rotation = rotation;
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
