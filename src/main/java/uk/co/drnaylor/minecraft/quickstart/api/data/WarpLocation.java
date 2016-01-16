package uk.co.drnaylor.minecraft.quickstart.api.data;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class WarpLocation {

    private final Location<World> location;

    private final Vector3d rotation;

    public WarpLocation(Location<World> location, Vector3d rotation) {
        this.location = location;
        this.rotation = rotation;
    }

    public Vector3d getRotation() {
        return rotation;
    }

    public Location<World> getLocation() {
        return location;
    }

}
